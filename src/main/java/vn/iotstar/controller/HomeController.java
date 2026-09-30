package vn.iotstar.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.UserService;

import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final vn.iotstar.service.ProductService productService;

    public HomeController(UserService userService, vn.iotstar.service.ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalProducts", productService.countAll());
        if (userDetails != null) model.addAttribute("ownProducts", productService.countByUser(userDetails.getId()));
        return "home";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("userCount", users.size());
        model.addAttribute("productCount", productService.countAll());
        return "admin";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            userService.getUserDtoByLogin(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("userProfile", u));
        }
        return "user/profile";
    }
}
