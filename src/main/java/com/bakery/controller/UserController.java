package com.bakery.controller;

import com.bakery.entity.User;
import com.bakery.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;  // IMPORT YANG DIPERBAIKI
import jakarta.servlet.http.HttpSession;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/users")  // ANOTASI YANG MEMBUTUHKAN IMPORT DI ATAS
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "users/profile";
        }
        return "redirect:/dashboard";
    }
    
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "users/edit-profile";
        }
        return "redirect:/dashboard";
    }
    
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "users/change-password";
        }
        return "redirect:/dashboard";
    }
}