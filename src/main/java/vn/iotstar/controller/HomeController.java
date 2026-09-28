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

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("totalUsers", userService.countUsers());
        return "home";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("userCount", users.size());
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
