package vn.iotstar.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetails (Ví dụ 2): Giữ đầy đủ thông tin người dùng
 * để hiển thị avatar, fullName, email, role trực tiếp trên giao diện
 * thông qua ${#authentication.principal.*}
 */
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String avatar;
    private final String roleName;
    private final boolean enabled;

    public CustomUserDetails(Long id, String username, String email, String password,
                              String fullName, String avatar, String roleName, boolean enabled) {
        this.id       = id;
        this.username = username;
        this.email    = email;
        this.password = password;
        this.fullName = fullName;
        this.avatar   = avatar;
        this.roleName = roleName;
        this.enabled  = enabled;
    }

    // ---- Getters for Thymeleaf: ${#authentication.principal.xxx} ----
    public Long getId()          { return id; }
    public String getEmail()     { return email; }
    public String getFullName()  { return fullName; }
    public String getAvatar()    { return avatar != null && !avatar.isBlank() ? avatar : "/images/avatar-default.png"; }
    public String getRoleName()  { return roleName; }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof CustomUserDetails user && java.util.Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() { return java.util.Objects.hashCode(id); }

    // ---- UserDetails interface ----
    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override public String getPassword()   { return password; }
    @Override public String getUsername()   { return username; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()    { return enabled; }
}
