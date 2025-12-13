package com.bakery.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterFormTest {

    @Test
    void testRegisterForm() {
        RegisterForm form = new RegisterForm();
        
        form.setName("Test User");
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        
        assertEquals("Test User", form.getName());
        assertEquals("test@example.com", form.getEmail());
        assertEquals("password123", form.getPassword());
        assertEquals("password123", form.getConfirmPassword());
    }
}