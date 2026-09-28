package vn.iotstar.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

/**
 * VD1 + VD2 kết hợp:
 *  - VD1: login bằng email
 *  - VD2: login bằng username hoặc email đều được
 * Trả về CustomUserDetails để Thymeleaf có thể dùng principal.fullName, principal.avatar, v.v.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản với username/email: " + login));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFullName(),
                user.getAvatar(),
                user.getRole().getName(),
                user.isEnabled()
        );
    }
}
