package vn.iotstar.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public class UserDTO {
    private Long id;
    @NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,60}")
    private String username;
    @NotBlank @Email @Size(max=150)
    private String email;
    @NotBlank @Size(max=200)
    private String fullName;
    private String avatar;
    @NotBlank @Pattern(regexp="ROLE_USER|ROLE_ADMIN")
    private String roleName;
    private boolean enabled;
    private LocalDateTime createdAt;
    private long productCount;
    public long getProductCount() { return productCount; }
    public void setProductCount(long count) { this.productCount=count; }

    public UserDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
