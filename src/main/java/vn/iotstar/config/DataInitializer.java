package vn.iotstar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            // Create or update Admin account with email: admin01@gmail.com
            User admin = userRepository.findByEmail("admin01@gmail.com")
                    .or(() -> userRepository.findByUsername("admin01"))
                    .orElseGet(User::new);
            admin.setUsername("admin01");
            admin.setEmail("admin01@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Quản Trị Viên Hệ Thống");
            admin.setAvatar("/images/admin.png");
            admin.setRole(adminRole);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info(">> Đã cập nhật Admin: username=admin01, email=admin01@gmail.com, pass=123456");

            // Create or update standard User account (Tên: Nguyễn Chiến)
            User user = userRepository.findByEmail("user01@gmail.com")
                    .or(() -> userRepository.findByUsername("user01"))
                    .orElseGet(User::new);
            user.setUsername("user01");
            user.setEmail("user01@gmail.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setFullName("Nguyễn Chiến");
            user.setAvatar("/images/user.png");
            user.setRole(userRole);
            user.setEnabled(true);
            userRepository.save(user);
            log.info(">> Đã cập nhật User: username=user01, fullName=Nguyễn Chiến, email=user01@gmail.com, pass=123456");
        };
    }
}
