package com.bakery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;  // Pastikan ini dari com.bakery.config
    
    public SecurityConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
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