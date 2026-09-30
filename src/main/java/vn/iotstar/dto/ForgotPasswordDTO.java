package vn.iotstar.dto;
import jakarta.validation.constraints.*;
public class ForgotPasswordDTO {
    @NotBlank @Email @Size(max=150)
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
