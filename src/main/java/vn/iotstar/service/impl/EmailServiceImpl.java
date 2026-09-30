package vn.iotstar.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;
@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender sender;
    private final String from;
    public EmailServiceImpl(JavaMailSender sender, @Value("${app.mail.from:}") String from) {
        this.sender=sender; this.from=from;
    }
    @Override public void sendOtp(String email, String otp, String subject) {
        if (from.isBlank()) throw new IllegalStateException("Chưa cấu hình email gửi OTP. Hãy cấu hình MAIL_USERNAME và MAIL_PASSWORD.");
        SimpleMailMessage mail=new SimpleMailMessage();
        mail.setFrom(from); mail.setTo(email); mail.setSubject(subject);
        mail.setText("Mã OTP của bạn: "+otp+"\nCó hiệu lực trong 5 phút, chỉ dùng một lần. Không chia sẻ mã này.");
        try { sender.send(mail); }
        catch (MailException ex) { throw new IllegalStateException("Không gửi được email. Vui lòng kiểm tra cấu hình SMTP hoặc thử lại.", ex); }
    }
}
