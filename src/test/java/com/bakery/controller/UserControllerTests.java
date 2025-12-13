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
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    private UserController userController;
    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        userId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");
    }

    @Test
    void testShowProfile_NoSession() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = userController.showProfile(model, session);

        assertEquals("redirect:/login", viewName);
        verify(userService, never()).getUserById(any());
    }

    @Test
    void testShowProfile_UserFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        String viewName = userController.showProfile(model, session);

        assertEquals("users/profile", viewName);
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void testShowProfile_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        String viewName = userController.showProfile(model, session);

        assertEquals("redirect:/dashboard", viewName);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void testShowEditProfileForm_NoSession() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = userController.showEditProfileForm(model, session);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testShowEditProfileForm_UserFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        String viewName = userController.showEditProfileForm(model, session);

        assertEquals("users/edit-profile", viewName);
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void testShowChangePasswordForm_NoSession() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = userController.showChangePasswordForm(model, session);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testShowChangePasswordForm_UserFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        String viewName = userController.showChangePasswordForm(model, session);

        assertEquals("users/change-password", viewName);
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void testShowEditProfileForm_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        String viewName = userController.showEditProfileForm(model, session);

        assertEquals("redirect:/dashboard", viewName);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void testShowChangePasswordForm_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        String viewName = userController.showChangePasswordForm(model, session);

        assertEquals("redirect:/dashboard", viewName);
        verify(model, never()).addAttribute(eq("user"), any());
    }
}
