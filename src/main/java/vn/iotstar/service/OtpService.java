package vn.iotstar.service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;

/** All operations run inside AuthService's transaction with the user row locked. */
@Service
@Transactional(propagation=Propagation.MANDATORY)
public class OtpService {
    public static final String REGISTER="REGISTER", RESET="RESET_PASSWORD";
    private final OtpTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService email;
    private final SecureRandom random=new SecureRandom();
    public OtpService(OtpTokenRepository tokens, PasswordEncoder encoder, EmailService email) {
        this.tokens=tokens; this.encoder=encoder; this.email=email;
    }
    public void send(String address, String type) {
        var now=LocalDateTime.now();
        OtpToken token=tokens.findByEmailAndType(address,type).orElseGet(OtpToken::new);
        if (token.getCreatedAt()!=null && token.getCreatedAt().plusSeconds(60).isAfter(now))
            throw new IllegalArgumentException("Vui lòng đợi 60 giây trước khi gửi lại OTP.");
        String code=String.format("%06d",random.nextInt(1_000_000));
        token.setEmail(address); token.setType(type); token.setOtpHash(encoder.encode(code));
        token.setCreatedAt(now); token.setExpiresAt(now.plusMinutes(5)); token.setAttempts(0); token.setUsed(false);
        tokens.saveAndFlush(token);
        email.sendOtp(address,code,REGISTER.equals(type) ? "SecurePortal - Xác nhận đăng ký" : "SecurePortal - Đặt lại mật khẩu");
    }
    public boolean verify(String address, String code, String type) {
        OtpToken token=tokens.findByEmailAndType(address,type).orElse(null);
        if (token==null || token.isUsed() || !token.getExpiresAt().isAfter(LocalDateTime.now()) || token.getAttempts()>=5)
            return false;
        token.setAttempts(token.getAttempts()+1);
        if (code==null || !encoder.matches(code,token.getOtpHash())) return false;
        token.setUsed(true);
        return true;
    }
}
