package com.bakery.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            String logMessage = String.format(
                "Request: %s %s | Status: %s | Duration: %dms",
                req.getMethod(),
                req.getRequestURI(),
                ((jakarta.servlet.http.HttpServletResponse) response).getStatus(),
                duration
            );
            
            logger.info(logMessage);
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestLoggingFilter initialized");
    }
    
    @Override
    public void destroy() {
        logger.info("RequestLoggingFilter destroyed");
    }
}