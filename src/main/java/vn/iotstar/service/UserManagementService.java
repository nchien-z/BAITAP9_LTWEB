package vn.iotstar.service;
import java.util.Locale;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.*;
import vn.iotstar.security.SessionInvalidator;
@Service
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly=true)
public class UserManagementService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final OtpTokenRepository tokens;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final SessionInvalidator sessions;
    public UserManagementService(UserRepository users,RoleRepository roles,ProductRepository products,
            OtpTokenRepository tokens,UserMapper mapper,PasswordEncoder encoder,SessionInvalidator sessions) {
        this.users=users;this.roles=roles;this.products=products;this.tokens=tokens;
        this.mapper=mapper;this.encoder=encoder;this.sessions=sessions;
    }
    private User findEntity(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy người dùng"));
    }
    private UserDTO dto(User user) {
        UserDTO dto=mapper.toDTO(user);dto.setProductCount(products.countByUserId(user.getId()));return dto;
    }
    public UserDTO find(Long id) { return dto(findEntity(id)); }
    public Page<UserDTO> search(String keyword,int page,int size) {
        return users.search(keyword==null?"":keyword,PageRequest.of(Math.max(0,page),Math.min(50,Math.max(1,size)),Sort.by("id").descending())).map(this::dto);
    }
    @Transactional
    public void save(Long id,UserDTO dto,Long actorId) {
        dto.setEmail(dto.getEmail().strip().toLowerCase(Locale.ROOT));
        users.findByUsernameIgnoreCase(dto.getUsername()).filter(u -> !u.getId().equals(id))
            .ifPresent(u -> { throw new IllegalArgumentException("Tên đăng nhập đã tồn tại."); });
        users.findByEmailIgnoreCase(dto.getEmail()).filter(u -> !u.getId().equals(id))
            .ifPresent(u -> { throw new IllegalArgumentException("Email đã tồn tại."); });
        if (id!=null && id.equals(actorId) && (!dto.isEnabled() || !"ROLE_ADMIN".equals(dto.getRoleName())))
            throw new IllegalArgumentException("Không thể tự khóa hoặc hạ quyền tài khoản đang sử dụng.");
        User user=id==null?mapper.toEntity(dto):findEntity(id);
        String oldEmail=user.getEmail();
        if (id!=null) mapper.update(dto,user);
        else {
            user.setPassword(encoder.encode("123456"));user.setAvatar("/images/avatar-default.png");
        }
        user.setRole(roles.findByName(dto.getRoleName()).orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ.")));
        user.setEmailVerified(true);
        if (oldEmail!=null && !oldEmail.equals(dto.getEmail())) tokens.deleteByEmail(oldEmail);
        users.saveAndFlush(user);
        if (id!=null) sessions.expire(id);
    }
    @Transactional
    public void delete(Long id,Long actorId) {
        if (id.equals(actorId)) throw new IllegalArgumentException("Không thể xóa chính tài khoản đang sử dụng.");
        User user=findEntity(id);
        if (products.countByUserId(id)>0) throw new IllegalArgumentException("Hãy xóa các sản phẩm của người dùng trước.");
        tokens.deleteByEmail(user.getEmail());users.delete(user);users.flush();sessions.expire(id);
    }
}
