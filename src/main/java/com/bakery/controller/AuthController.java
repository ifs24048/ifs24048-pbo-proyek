package com.bakery.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bakery.entity.User;
import com.bakery.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
    
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {
        
        Optional<User> user = userService.login(email, password);
        if (user.isPresent()) {
            session.setAttribute("userId", user.get().getId());
            session.setAttribute("userName", user.get().getName());
            session.setAttribute("userEmail", user.get().getEmail());
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Email atau password salah");
            return "auth/login";
        }
    }
    
    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        
        try {
            userService.createUser(name, email, password);
            return "redirect:/login?success=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}