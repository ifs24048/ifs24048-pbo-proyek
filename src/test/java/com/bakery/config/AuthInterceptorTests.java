package com.bakery.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
    }

    @Test
    void testPreHandle_PublicResource_ReturnsTrue() throws Exception {
        // Test various static resources and public paths
        String[] publicPaths = {
                "/static/css/style.css",
                "/uploads/product.jpg",
                "/h2-console/login",
                "/",
                "/login",
                "/register",
                "/about",
                "/script.js",
                "/image.png",
                "/icon.ico",
                "/favicon.ico",
                "/styles.css", // Explicitly cover .css suffix
                "/photo.jpg" // Explicitly cover .jpg suffix
        };

        for (String path : publicPaths) {
            when(request.getRequestURI()).thenReturn(path);
            boolean result = authInterceptor.preHandle(request, response, null);
            assertTrue(result, "Path should be public: " + path);
        }
    }

    @Test
    void testPreHandle_NoSession_RedirectsToLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).sendRedirect("/login");
    }

    @Test
    void testPreHandle_InvalidSession_RedirectsToLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).sendRedirect("/login");
    }

    @Test
    void testPreHandle_ValidSession_ReturnsTrue() throws Exception {
        UUID userId = UUID.randomUUID();

        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testPreHandle_InvalidUserIdInSession_RedirectsToLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn("invalid-uuid");

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).sendRedirect("/login");
        verify(session).invalidate();
    }

    @Test
    void testPreHandle_ValidSessionNullUserId_RedirectsToLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);

        // Return UUID first (pass line 28), then null (fail line 36)
        when(session.getAttribute("userId"))
                .thenReturn(UUID.randomUUID())
                .thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).sendRedirect("/login");
        verify(session).invalidate();
    }

    @Test
    void testIsPublicResource() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();

        // Use reflection to test private method
        var method = AuthInterceptor.class.getDeclaredMethod("isPublicResource", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(interceptor, "/static/css/style.css"));
        assertTrue((Boolean) method.invoke(interceptor, "/uploads/image.jpg"));
        assertTrue((Boolean) method.invoke(interceptor, "/login"));
        assertTrue((Boolean) method.invoke(interceptor, "/register"));
        assertTrue((Boolean) method.invoke(interceptor, "/about"));
        assertTrue((Boolean) method.invoke(interceptor, "/favicon.ico"));
        assertTrue((Boolean) method.invoke(interceptor, "/test.css"));
        assertTrue((Boolean) method.invoke(interceptor, "/test.js"));
        assertTrue((Boolean) method.invoke(interceptor, "/test.png"));
        assertTrue((Boolean) method.invoke(interceptor, "/test.jpg"));
        assertTrue((Boolean) method.invoke(interceptor, "/test.ico"));

        assertFalse((Boolean) method.invoke(interceptor, "/dashboard"));
        assertFalse((Boolean) method.invoke(interceptor, "/products"));
    }
}