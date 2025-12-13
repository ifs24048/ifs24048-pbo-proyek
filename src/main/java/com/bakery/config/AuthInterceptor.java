package com.bakery.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();

        // Allow access to public pages without authentication
        if (isPublicResource(requestURI)) {
            return true;
        }

        // Check if user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Additional validation
        try {
            UUID userId = (UUID) session.getAttribute("userId");
            if (userId == null) {
                session.invalidate();
                response.sendRedirect("/login");
                return false;
            }
        } catch (Exception e) {
            session.invalidate();
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }

    private boolean isPublicResource(String requestURI) {
        return requestURI.startsWith("/static/") ||
                requestURI.startsWith("/uploads/") ||
                requestURI.startsWith("/h2-console/") ||
                requestURI.equals("/") ||
                requestURI.equals("/login") ||
                requestURI.equals("/register") ||
                requestURI.equals("/about") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".png") ||
                requestURI.endsWith(".jpg") ||
                requestURI.endsWith(".ico");
    }
}