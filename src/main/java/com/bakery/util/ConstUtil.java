package com.bakery.util;

public class ConstUtil {
    
    // File upload constants
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_FILE_TYPES = {
        "image/jpeg", "image/png", "image/gif"
    };
    
    // Product categories
    public static final String[] PRODUCT_CATEGORIES = {
        "Kue", "Roti", "Pastry", "Cookies", "Donat", "Pie", "Tart"
    };
    
    // Session constants
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_EMAIL = "userEmail";
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    // Validation messages
    public static final String REQUIRED_FIELD = "Field ini wajib diisi";
    public static final String INVALID_EMAIL = "Format email tidak valid";
    public static final String PASSWORD_MIN_LENGTH = "Password minimal 6 karakter";
    
    private ConstUtil() {
        // Utility class, prevent instantiation
    }
}