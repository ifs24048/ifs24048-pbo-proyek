package com.bakery.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginFormTest {

    @Test
    void testLoginForm() {
        LoginForm form = new LoginForm();
        
        form.setEmail("test@example.com");
        form.setPassword("password123");
        
        assertEquals("test@example.com", form.getEmail());
        assertEquals("password123", form.getPassword());
    }

    @Test
    void testLoginFormEmpty() {
        LoginForm form = new LoginForm();
        
        assertNull(form.getEmail());
        assertNull(form.getPassword());
    }
}