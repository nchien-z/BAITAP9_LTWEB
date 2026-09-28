package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);

    /** Đăng nhập bằng username HOẶC email (VD2) */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :login OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);
}
