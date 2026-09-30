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
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> lockByEmail(@Param("email") String email);

    @Query("select u from User u where lower(u.username) like lower(concat('%', :keyword, '%')) "
         + "or lower(u.email) like lower(concat('%', :keyword, '%')) "
         + "or lower(u.fullName) like lower(concat('%', :keyword, '%'))")
    org.springframework.data.domain.Page<User> search(@Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);

    /** Đăng nhập bằng username HOẶC email (VD2) */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :login OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);
}
