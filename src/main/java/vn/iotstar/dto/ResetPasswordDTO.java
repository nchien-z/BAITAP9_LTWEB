package vn.iotstar.dto;
import jakarta.validation.constraints.*;
public class ResetPasswordDTO {
    @NotBlank @Email @Size(max=150)
    private String email;
    @NotBlank @Pattern(regexp="[0-9]{6}")
    private String otp;
    @NotBlank @Size(min=6, max=72)
    private String password;
    @NotBlank
    private String confirmPassword;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

}
