package com.bakery.controller;

import com.bakery.entity.User;
import com.bakery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService);
    }

    @Test
    void testShowLoginForm() {
        String viewName = authController.showLoginForm();
        assertEquals("auth/login", viewName);
    }

    @Test
    void testShowRegisterForm() {
        String viewName = authController.showRegisterForm();
        assertEquals("auth/register", viewName);
    }

    @Test
    void testLogin_Success() {
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail(email);

        when(userService.login(email, password)).thenReturn(Optional.of(user));

        String viewName = authController.login(email, password, model, session);

        assertEquals("redirect:/dashboard", viewName);
        verify(session).setAttribute("userId", user.getId());
        verify(session).setAttribute("userName", user.getName());
        verify(session).setAttribute("userEmail", user.getEmail());
        verify(model, never()).addAttribute(eq("error"), anyString());
    }

    @Test
    void testLogin_Failure() {
        String email = "test@example.com";
        String password = "wrongpassword";

        when(userService.login(email, password)).thenReturn(Optional.empty());

        String viewName = authController.login(email, password, model, session);

        assertEquals("auth/login", viewName);
        verify(model).addAttribute("error", "Email atau password salah");
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void testRegister_Success() {
        String name = "Test User";
        String email = "test@example.com";
        String password = "password";

        doNothing().when(userService).createUser(name, email, password);

        String viewName = authController.register(name, email, password, model);

        assertEquals("redirect:/login?success=true", viewName);
        verify(userService).createUser(name, email, password);
        verify(model, never()).addAttribute(eq("error"), anyString());
    }

    @Test
    void testRegister_Failure() {
        String name = "Test User";
        String email = "test@example.com";
        String password = "password";
        String errorMessage = "Email sudah terdaftar";

        doThrow(new RuntimeException(errorMessage)).when(userService).createUser(name, email, password);

        String viewName = authController.register(name, email, password, model);

        assertEquals("auth/register", viewName);
        verify(model).addAttribute("error", errorMessage);
    }

    @Test
    void testLogout() {
        String viewName = authController.logout(session);
        
        assertEquals("redirect:/login", viewName);
        verify(session).invalidate();
    }
}