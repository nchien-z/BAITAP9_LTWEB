package vn.iotstar.service;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.User;
import vn.iotstar.repository.*;
import vn.iotstar.security.SessionInvalidator;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final OtpService otp;
    private final PasswordEncoder encoder;
    private final SessionInvalidator sessions;
    public AuthService(UserRepository users, RoleRepository roles, OtpService otp,
            PasswordEncoder encoder, SessionInvalidator sessions) {
        this.users=users; this.roles=roles; this.otp=otp; this.encoder=encoder; this.sessions=sessions;
    }
    private String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    private void checkPassword(String password,String confirm) {
        if (!password.equals(confirm)) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        if (password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Mật khẩu tối đa 72 byte UTF-8.");
    }
    @Transactional
    public void register(RegisterDTO dto) {
        checkPassword(dto.getPassword(),dto.getConfirmPassword());
        String email=normalize(dto.getEmail());
        if (users.findByUsernameIgnoreCase(dto.getUsername()).isPresent()) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email đã tồn tại. Nếu chưa xác thực, hãy gửi lại OTP.");
        User user=new User();
        user.setUsername(dto.getUsername()); user.setEmail(email); user.setFullName(dto.getFullName().strip());
        user.setPassword(encoder.encode(dto.getPassword())); user.setAvatar("/images/avatar-default.png");
        user.setRole(roles.findByName("ROLE_USER").orElseThrow());
        user.setEnabled(false); user.setEmailVerified(false);
        users.saveAndFlush(user);
        otp.send(email,OtpService.REGISTER);
    }
    @Transactional
    public void resend(String address) {
        User user=users.lockByEmail(normalize(address)).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (user.isEmailVerified()) throw new IllegalArgumentException("Email đã được xác thực.");
        otp.send(user.getEmail(),OtpService.REGISTER);
    }
    @Transactional
    public boolean verifyRegister(String address,String code) {
        User user=users.lockByEmail(normalize(address)).orElse(null);
        if (user==null || user.isEmailVerified() || !otp.verify(user.getEmail(),code,OtpService.REGISTER)) return false;
        user.setEmailVerified(true); user.setEnabled(true); return true;
    }
    @Transactional
    public void forgotPassword(String address) {
        User user=users.lockByEmail(normalize(address)).orElse(null);
        if (user!=null && user.isEmailVerified() && user.isEnabled()) otp.send(user.getEmail(),OtpService.RESET);
    }
    @Transactional
    public boolean resetPassword(ResetPasswordDTO dto) {
        checkPassword(dto.getPassword(),dto.getConfirmPassword());
        User user=users.lockByEmail(normalize(dto.getEmail())).orElse(null);
        if (user==null || !user.isEmailVerified() || !user.isEnabled()
            || !otp.verify(user.getEmail(),dto.getOtp(),OtpService.RESET)) return false;
        user.setPassword(encoder.encode(dto.getPassword()));
        sessions.expire(user.getId());
        return true;
    }
}
