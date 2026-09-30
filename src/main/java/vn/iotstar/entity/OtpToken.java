package vn.iotstar.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="otp_tokens", uniqueConstraints=@UniqueConstraint(columnNames={"email","type"}))
public class OtpToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=150)
    private String email;
    @Column(nullable=false, length=30)
    private String type;
    @Column(nullable=false, length=100)
    private String otpHash;
    @Column(nullable=false)
    private LocalDateTime expiresAt;
    @Column(nullable=false)
    private LocalDateTime createdAt;
    @Column(nullable=false)
    private int attempts;
    @Column(nullable=false)
    private boolean used;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

}
