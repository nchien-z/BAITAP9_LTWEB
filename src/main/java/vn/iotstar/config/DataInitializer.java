package vn.iotstar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initDatabase(RoleRepository roles, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            Role userRole = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
            Role adminRole = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));
            seed(users, encoder, adminRole, "admin01", "admin01@gmail.com", "Quản Trị Viên Hệ Thống", "/images/admin.png");
            seed(users, encoder, userRole, "user01", "user01@gmail.com", "Nguyễn Chiến", "/images/user.png");
        };
    }
    private void seed(UserRepository users, PasswordEncoder encoder, Role role,
                      String username, String email, String name, String avatar) {
        // Seed once; never overwrite an existing password or account status.
        if (users.findByUsername(username).isPresent() || users.existsByEmailIgnoreCase(email)) return;
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode("123456"));
        user.setFullName(name);
        user.setAvatar(avatar);
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(true);
        users.save(user);
    }
}
