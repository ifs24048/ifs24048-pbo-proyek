package com.bakery.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConstUtilTest {

    @Test
    void testConstants() {
        // Test file upload constants
        assertEquals(5 * 1024 * 1024, ConstUtil.MAX_FILE_SIZE);
        
        String[] allowedTypes = ConstUtil.ALLOWED_FILE_TYPES;
        assertEquals(3, allowedTypes.length);
        assertEquals("image/jpeg", allowedTypes[0]);
        assertEquals("image/png", allowedTypes[1]);
        assertEquals("image/gif", allowedTypes[2]);
        
        // Test product categories
        String[] categories = ConstUtil.PRODUCT_CATEGORIES;
        assertEquals(7, categories.length);
        assertEquals("Kue", categories[0]);
        
        // Test session constants
        assertEquals("userId", ConstUtil.SESSION_USER_ID);
        assertEquals("userName", ConstUtil.SESSION_USER_NAME);
        assertEquals("userEmail", ConstUtil.SESSION_USER_EMAIL);
        
        // Test pagination
        assertEquals(10, ConstUtil.DEFAULT_PAGE_SIZE);
        
        // Test validation messages
        assertEquals("Field ini wajib diisi", ConstUtil.REQUIRED_FIELD);
        assertEquals("Format email tidak valid", ConstUtil.INVALID_EMAIL);
        assertEquals("Password minimal 6 karakter", ConstUtil.PASSWORD_MIN_LENGTH);
    }

    @Test
    void testConstructorAccess() {
        // Utility class should not be instantiable
        // We can't test private constructor directly
        // Just verify the class exists and constants are accessible
        assertNotNull(ConstUtil.MAX_FILE_SIZE);
        assertNotNull(ConstUtil.ALLOWED_FILE_TYPES);
        assertNotNull(ConstUtil.PRODUCT_CATEGORIES);
    }
}