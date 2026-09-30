package vn.iotstar.controller;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.UserManagementService;
@Controller
@RequestMapping("/users")
public class UserManagementController {
    private final UserManagementService users;
    public UserManagementController(UserManagementService users) { this.users=users; }
    @GetMapping public String list(@RequestParam(defaultValue="") String keyword,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,Model model) {
        var result=users.search(keyword,page,size);model.addAttribute("users",result);
        model.addAttribute("keyword",keyword);model.addAttribute("size",result.getSize());return "users/list";
    }
    @GetMapping("/create") public String create(Model model) {
        UserDTO dto=new UserDTO();dto.setRoleName("ROLE_USER");dto.setEnabled(true);
        model.addAttribute("userDTO",dto);model.addAttribute("mode","create");return "users/form";
    }
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id,Model model) {
        model.addAttribute("userDTO",users.find(id));model.addAttribute("mode","edit");return "users/form";
    }
    @PostMapping({"/create","/edit/{id}"}) public String save(@PathVariable(required=false) Long id,
            @Valid @ModelAttribute("userDTO") UserDTO dto,BindingResult errors,@AuthenticationPrincipal CustomUserDetails actor,
            Model model,RedirectAttributes redirect) {
        dto.setId(id);model.addAttribute("mode",id==null?"create":"edit");
        if (errors.hasErrors()) return "users/form";
        try { users.save(id,dto,actor.getId()); }
        catch (IllegalArgumentException ex) { errors.reject("user",ex.getMessage());return "users/form"; }
        catch (DataIntegrityViolationException ex) { errors.reject("duplicate","Tên đăng nhập hoặc email đã tồn tại.");return "users/form"; }
        redirect.addFlashAttribute("successMessage",id==null?"Đã tạo người dùng. Mật khẩu ban đầu: 123456":"Đã cập nhật người dùng.");
        return "redirect:/users";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetails actor,RedirectAttributes redirect) {
        try { users.delete(id,actor.getId());redirect.addFlashAttribute("successMessage","Đã xóa người dùng."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("errorMessage",ex.getMessage()); }
        catch (DataIntegrityViolationException ex) { redirect.addFlashAttribute("errorMessage","Không xóa được vì người dùng còn dữ liệu liên quan."); }
        return "redirect:/users";
    }
}
