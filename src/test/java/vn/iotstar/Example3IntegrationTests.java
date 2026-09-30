package vn.iotstar;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.*;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import vn.iotstar.security.*;
import vn.iotstar.service.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

@SpringBootTest(properties={
    "spring.config.import=",
    "spring.datasource.url=jdbc:h2:mem:example3;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(Example3IntegrationTests.Fakes.class)
class Example3IntegrationTests {
    @TestConfiguration static class Fakes {
        @Bean @Primary FakeEmail fakeEmail() { return new FakeEmail(); }
        @Bean @Primary FakeImages fakeImages() { return new FakeImages(); }
    }
    static class FakeEmail implements EmailService {
        String code;
        boolean fail;
        @Override public void sendOtp(String email,String otp,String subject) {
            if (fail) throw new IllegalStateException("SMTP unavailable");
            code=otp;
        }
    }
    static class FakeImages implements CloudinaryService {
        int counter;
        List<String> removed=new ArrayList<>();
        @Override public UploadResult upload(MultipartFile file) {
            return new UploadResult("https://example.com/image.png","test-image-"+(++counter));
        }
        @Override public void delete(String id) { removed.add(id); }
    }
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired OtpTokenRepository tokens;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired AuthService auth;
    @Autowired FakeEmail email;
    @Autowired FakeImages images;
    @Autowired CustomUserDetailsService details;
    @Autowired org.springframework.boot.CommandLineRunner initDatabase;
    User owner,other,admin;

    @BeforeEach void setUp() {
        SecurityContextHolder.clearContext();
        products.deleteAll();tokens.deleteAll();users.deleteAll();
        email.code=null;email.fail=false;images.counter=0;images.removed.clear();
        owner=createUser("owner","ROLE_USER");other=createUser("other","ROLE_USER");admin=createUser("manager","ROLE_ADMIN");
    }
    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }
    User createUser(String name,String role) {
        User u=new User();u.setUsername(name);u.setEmail(name+"@example.com");u.setFullName("Nguyễn "+name);
        u.setPassword(encoder.encode("123456"));u.setRole(roles.findByName(role).orElseThrow());
        u.setAvatar("/images/user.png");u.setEnabled(true);u.setEmailVerified(true);return users.saveAndFlush(u);
    }
    CustomUserDetails principal(User user) { return (CustomUserDetails)details.loadUserByUsername(user.getUsername()); }
    RegisterDTO registration() {
        RegisterDTO dto=new RegisterDTO();dto.setUsername("newuser");dto.setEmail("new@example.com");
        dto.setFullName("Người dùng mới");dto.setPassword("Password123!");dto.setConfirmPassword("Password123!");return dto;
    }
    Product product(User user,String name) {
        Product p=new Product();p.setName(name);p.setDescription("Mô tả");p.setPrice(new BigDecimal("125000.50"));p.setUser(user);
        return products.saveAndFlush(p);
    }
    @Test void registrationActivationAndSessionLogin() throws Exception {
        mvc.perform(post("/register").with(csrf()).param("username","newuser").param("email","new@example.com")
            .param("fullName","Người dùng mới").param("password","Password123!").param("confirmPassword","Password123!"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/verify-otp?email=new%40example.com"));
        User u=users.findByEmail("new@example.com").orElseThrow();
        assertThat(u.isEnabled()).isFalse();
        assertThat(encoder.matches("Password123!",u.getPassword())).isTrue();
        var otp=tokens.findByEmailAndType(u.getEmail(),OtpService.REGISTER).orElseThrow();
        assertThat(otp.getOtpHash()).isNotEqualTo(email.code);
        mvc.perform(post("/login").with(csrf()).param("username","newuser").param("password","Password123!")).andExpect(unauthenticated());
        mvc.perform(post("/verify-otp").with(csrf()).param("email",u.getEmail()).param("otp",email.code))
            .andExpect(redirectedUrl("/login"));
        assertThat(auth.verifyRegister(u.getEmail(),email.code)).isFalse();
        var result=mvc.perform(post("/login").with(csrf()).param("username","newuser").param("password","Password123!"))
            .andExpect(authenticated()).andExpect(redirectedUrl("/home")).andReturn();
        var session=(MockHttpSession)result.getRequest().getSession(false);
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Người dùng mới")));
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout=true")).andExpect(unauthenticated());
    }
    @Test void loginStillSupportsEmailAndUsername() throws Exception {
        for (String login:List.of("owner","owner@example.com"))
            mvc.perform(post("/login").with(csrf()).param("username",login).param("password","123456"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/home"));
    }
    @Test void invalidOtpAttemptsAreCommittedAndBlockCorrectCode() {
        auth.register(registration());
        String correct=email.code, wrong=correct.equals("000000")?"111111":"000000";
        for (int i=0;i<5;i++) assertThat(auth.verifyRegister("new@example.com",wrong)).isFalse();
        assertThat(tokens.findByEmailAndType("new@example.com",OtpService.REGISTER).orElseThrow().getAttempts()).isEqualTo(5);
        assertThat(auth.verifyRegister("new@example.com",correct)).isFalse();
    }
    @Test void expirationResendCooldownAndPurposeIsolation() {
        auth.register(registration());
        assertThatThrownBy(() -> auth.resend("new@example.com")).isInstanceOf(IllegalArgumentException.class);
        var token=tokens.findByEmailAndType("new@example.com",OtpService.REGISTER).orElseThrow();
        token.setCreatedAt(LocalDateTime.now().minusMinutes(6));token.setExpiresAt(LocalDateTime.now().minusMinutes(1));tokens.saveAndFlush(token);
        assertThat(auth.verifyRegister("new@example.com",email.code)).isFalse();
        auth.resend("new@example.com");
        assertThat(auth.verifyRegister("new@example.com",email.code)).isTrue();
        auth.forgotPassword("new@example.com");
        assertThat(auth.verifyRegister("new@example.com",email.code)).isFalse();
    }
    @Test void resetPasswordRequiresValidSingleUseOtpAndExpiresSession() throws Exception {
        var login=mvc.perform(post("/login").with(csrf()).param("username","owner").param("password","123456")).andReturn();
        var session=(MockHttpSession)login.getRequest().getSession(false);
        ResetPasswordDTO dto=new ResetPasswordDTO();dto.setEmail(owner.getEmail());dto.setPassword("NewPassword123!");dto.setConfirmPassword(dto.getPassword());dto.setOtp("123456");
        assertThat(auth.resetPassword(dto)).isFalse();
        auth.forgotPassword(owner.getEmail());dto.setOtp(email.code);
        assertThat(auth.resetPassword(dto)).isTrue();
        assertThat(auth.resetPassword(dto)).isFalse();
        assertThat(encoder.matches(dto.getPassword(),users.findById(owner.getId()).orElseThrow().getPassword())).isTrue();
        mvc.perform(get("/profile").session(session)).andExpect(content().string(org.hamcrest.Matchers.containsString("expired")));
        mvc.perform(post("/login").with(csrf()).param("username","owner").param("password","123456")).andExpect(unauthenticated());
        mvc.perform(post("/login").with(csrf()).param("username","owner").param("password",dto.getPassword())).andExpect(authenticated());
    }
    @Test void mailFailureRollsBackRegistrationAndDisabledAccountCannotReactivate() {
        email.fail=true;
        assertThatThrownBy(() -> auth.register(registration())).isInstanceOf(IllegalStateException.class);
        assertThat(users.findByEmail("new@example.com")).isEmpty();assertThat(tokens.count()).isZero();
        owner.setEnabled(false);users.saveAndFlush(owner);email.fail=false;
        assertThatThrownBy(() -> auth.resend(owner.getEmail())).isInstanceOf(IllegalArgumentException.class);
        auth.forgotPassword(owner.getEmail());assertThat(tokens.count()).isZero();
    }
    @Test void publicFormsRenderAndCsrfProtectsMutations() throws Exception {
        for (String path:List.of("/login","/register","/verify-otp","/forgot-password","/reset-password","/home"))
            mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(post("/register")).andExpect(status().isForbidden());
        mvc.perform(post("/register").with(csrf()).param("email","bad")).andExpect(status().isOk()).andExpect(model().hasErrors());
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/users").with(user(principal(owner)))).andExpect(status().isForbidden());
    }
    @Test void productCrudOwnershipAndImageLifecycle() throws Exception {
        Product foreign=product(other,"Sản phẩm khác");
        mvc.perform(get("/products/edit/"+foreign.getId()).with(user(principal(owner)))).andExpect(status().isForbidden());
        mvc.perform(post("/products/delete/"+foreign.getId()).with(user(principal(owner))).with(csrf())).andExpect(status().isForbidden());
        var file=new MockMultipartFile("image","image.png","image/png",new byte[]{1,2,3});
        mvc.perform(multipart("/products/create").file(file).with(user(principal(owner))).with(csrf())
            .param("name","Sản phẩm mới").param("price","123.50").param("userId",other.getId().toString()))
            .andExpect(redirectedUrl("/products"));
        Product created=products.findAll().stream().filter(p -> p.getName().equals("Sản phẩm mới")).findFirst().orElseThrow();
        assertThat(created.getUser().getId()).isEqualTo(owner.getId());assertThat(created.getImagePublicId()).isEqualTo("test-image-1");
        mvc.perform(multipart("/products/edit/"+created.getId()).file(file).with(user(principal(owner))).with(csrf())
            .param("name","Đã cập nhật").param("price","200"))
            .andExpect(redirectedUrl("/products"));
        assertThat(images.removed).contains("test-image-1");
        mvc.perform(post("/products/delete/"+created.getId()).with(user(principal(owner))).with(csrf())).andExpect(redirectedUrl("/products"));
        assertThat(images.removed).contains("test-image-2");
        assertThat(products.existsById(created.getId())).isFalse();
    }
    @Test void productSearchPaginationCountsAndForms() throws Exception {
        product(owner,"Laptop A");product(owner,"Laptop B");product(other,"Laptop C");
        mvc.perform(get("/products").with(user(principal(owner))).param("keyword","Laptop").param("size","1"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("2 kết quả")));
        mvc.perform(get("/products").with(user(principal(admin))).param("keyword","Laptop"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("3 kết quả")));
        mvc.perform(get("/products/create").with(user(principal(owner)))).andExpect(status().isOk());
        mvc.perform(get("/products/edit/"+products.findAll().get(0).getId()).with(user(principal(admin)))).andExpect(status().isOk());
        mvc.perform(get("/home").with(user(principal(owner)))).andExpect(status().isOk()).andExpect(model().attribute("ownProducts",2L));
        mvc.perform(post("/products/create").with(user(principal(owner))).with(csrf()).param("name","").param("price","-1"))
            .andExpect(status().isOk()).andExpect(model().hasErrors());
    }
    @Test void adminUserCrudDuplicateValidationAndProductCount() throws Exception {
        mvc.perform(post("/users/create").with(user(principal(admin))).with(csrf())
            .param("username","created").param("email","created@example.com").param("fullName","Người mới")
            .param("roleName","ROLE_USER").param("enabled","true")).andExpect(redirectedUrl("/users"));
        User created=users.findByUsername("created").orElseThrow();
        assertThat(encoder.matches("123456",created.getPassword())).isTrue();
        mvc.perform(post("/users/edit/"+created.getId()).with(user(principal(admin))).with(csrf())
            .param("username","updated").param("email","updated@example.com").param("fullName","Đã sửa")
            .param("roleName","ROLE_USER").param("enabled","true")).andExpect(redirectedUrl("/users"));
        mvc.perform(post("/users/edit/"+created.getId()).with(user(principal(admin))).with(csrf())
            .param("username","owner").param("email","updated@example.com").param("fullName","Trùng")
            .param("roleName","ROLE_USER")).andExpect(status().isOk()).andExpect(model().hasErrors());
        mvc.perform(get("/users/create").with(user(principal(admin)))).andExpect(status().isOk());
        mvc.perform(get("/users/edit/"+created.getId()).with(user(principal(admin)))).andExpect(status().isOk());
        product(created,"Sản phẩm");
        mvc.perform(get("/users").with(user(principal(admin))).param("keyword","updated").param("size","1"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("1 kết quả")));
        mvc.perform(post("/users/delete/"+created.getId()).with(user(principal(admin))).with(csrf()))
            .andExpect(redirectedUrl("/users")).andExpect(flash().attributeExists("errorMessage"));
        products.deleteAll();
        mvc.perform(post("/users/delete/"+created.getId()).with(user(principal(admin))).with(csrf())).andExpect(redirectedUrl("/users"));
        assertThat(users.existsById(created.getId())).isFalse();
        mvc.perform(post("/users/delete/"+admin.getId()).with(user(principal(admin))).with(csrf()))
            .andExpect(flash().attributeExists("errorMessage"));
    }
    @Test void initializerDoesNotOverwriteChangedPassword() throws Exception {
        User seeded=createUser("admin01","ROLE_ADMIN");
        seeded.setEmail("admin01@gmail.com");seeded.setPassword(encoder.encode("changed-secret"));users.saveAndFlush(seeded);
        initDatabase.run();
        assertThat(encoder.matches("changed-secret",users.findByUsername("admin01").orElseThrow().getPassword())).isTrue();
    }
}
