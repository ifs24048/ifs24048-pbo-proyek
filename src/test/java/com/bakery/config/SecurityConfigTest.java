package com.bakery.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Test
    void testAddInterceptors() {
        when(registry.addInterceptor(authInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns("/**")).thenReturn(interceptorRegistration);

        SecurityConfig securityConfig = new SecurityConfig(authInterceptor);
        securityConfig.addInterceptors(registry);

        verify(registry).addInterceptor(authInterceptor);
        verify(interceptorRegistration).addPathPatterns("/**");
        verify(interceptorRegistration).excludePathPatterns(
            "/static/**",
            "/uploads/**",
            "/h2-console/**",
            "/",
            "/login",
            "/register",
            "/about",
            "/error",
            "/favicon.ico"
        );
    }
}