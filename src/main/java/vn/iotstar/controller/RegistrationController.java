package vn.iotstar.controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import vn.iotstar.dto.*;
import vn.iotstar.service.AuthService;
@Controller
public class RegistrationController {
    private final AuthService auth;
    public RegistrationController(AuthService auth) { this.auth=auth; }
    @GetMapping("/register") public String register(Model model) {
        model.addAttribute("registerDTO",new RegisterDTO());return "auth/register";
    }
    @PostMapping("/register") public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult errors,RedirectAttributes redirect) {
        if (errors.hasErrors()) return "auth/register";
        try { auth.register(dto); }
        catch (IllegalArgumentException | IllegalStateException ex) { errors.reject("register",ex.getMessage());return "auth/register"; }
        catch (DataIntegrityViolationException ex) { errors.reject("duplicate","Tên đăng nhập hoặc email đã tồn tại.");return "auth/register"; }
        redirect.addAttribute("email",dto.getEmail());
        redirect.addFlashAttribute("successMessage","Đã gửi OTP. Kiểm tra hộp thư để xác nhận tài khoản.");
        return "redirect:/verify-otp";
    }
    @GetMapping("/verify-otp") public String verify(@RequestParam(defaultValue="") String email,Model model) {
        VerifyOtpDTO dto=new VerifyOtpDTO();dto.setEmail(email);model.addAttribute("verifyOtpDTO",dto);return "auth/verify-otp";
    }
    @PostMapping("/verify-otp") public String verify(@Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto,
            BindingResult errors,RedirectAttributes redirect) {
        if (errors.hasErrors()) return "auth/verify-otp";
        if (!auth.verifyRegister(dto.getEmail(),dto.getOtp())) {
            errors.reject("otp","OTP không đúng, đã hết hạn, đã sử dụng hoặc vượt số lần thử.");return "auth/verify-otp";
        }
        redirect.addFlashAttribute("successMessage","Xác thực thành công. Bạn có thể đăng nhập.");
        return "redirect:/login";
    }
    @PostMapping("/resend-register-otp") public String resend(@Valid @ModelAttribute ForgotPasswordDTO dto,
            BindingResult errors,RedirectAttributes redirect) {
        redirect.addAttribute("email",dto.getEmail());
        if (errors.hasErrors()) redirect.addFlashAttribute("errorMessage","Email không hợp lệ.");
        else try { auth.resend(dto.getEmail());redirect.addFlashAttribute("successMessage","Đã gửi lại OTP."); }
        catch (IllegalArgumentException | IllegalStateException ex) { redirect.addFlashAttribute("errorMessage",ex.getMessage()); }
        return "redirect:/verify-otp";
    }
    @GetMapping("/forgot-password") public String forgot(Model model) {
        model.addAttribute("forgotPasswordDTO",new ForgotPasswordDTO());return "auth/forgot-password";
    }
    @PostMapping("/forgot-password") public String forgot(@Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
            BindingResult errors,RedirectAttributes redirect) {
        if (errors.hasErrors()) return "auth/forgot-password";
        try { auth.forgotPassword(dto.getEmail()); }
        catch (IllegalArgumentException | IllegalStateException ex) { errors.reject("mail",ex.getMessage());return "auth/forgot-password"; }
        redirect.addAttribute("email",dto.getEmail());
        redirect.addFlashAttribute("successMessage","Nếu email thuộc tài khoản đang hoạt động, mã OTP đã được gửi.");
        return "redirect:/reset-password";
    }
    @GetMapping("/reset-password") public String reset(@RequestParam(defaultValue="") String email,Model model) {
        ResetPasswordDTO dto=new ResetPasswordDTO();dto.setEmail(email);model.addAttribute("resetPasswordDTO",dto);return "auth/reset-password";
    }
    @PostMapping("/reset-password") public String reset(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
            BindingResult errors,RedirectAttributes redirect) {
        if (errors.hasErrors()) return "auth/reset-password";
        try {
            if (!auth.resetPassword(dto)) { errors.reject("otp","OTP không đúng, hết hạn hoặc đã sử dụng.");return "auth/reset-password"; }
        } catch (IllegalArgumentException ex) { errors.reject("password",ex.getMessage());return "auth/reset-password"; }
        redirect.addFlashAttribute("successMessage","Đã đổi mật khẩu. Vui lòng đăng nhập lại.");
        return "redirect:/login";
    }
}
