package vn.iotstar.dto;
import jakarta.validation.constraints.*;
public class RegisterDTO {
    @NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,60}", message="Tên đăng nhập 3–60 ký tự, chỉ gồm chữ, số, dấu . _ -")
    private String username;
    @NotBlank @Email @Size(max=150)
    private String email;
    @NotBlank @Size(min=6, max=72)
    private String password;
    @NotBlank
    private String confirmPassword;
    @NotBlank @Size(max=200)
    private String fullName;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

}
