package com.bakery.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private ResourceHandlerRegistry registry;

    @Mock
    private ResourceHandlerRegistration uploadsRegistration;

    @Mock
    private ResourceHandlerRegistration staticRegistration;

    @Mock
    private ResourceHandlerRegistration h2ConsoleRegistration;

    @Test
    void testAddResourceHandlers() {
        when(registry.addResourceHandler("/uploads/**")).thenReturn(uploadsRegistration);
        when(registry.addResourceHandler("/static/**")).thenReturn(staticRegistration);
        when(registry.addResourceHandler("/h2-console/**")).thenReturn(h2ConsoleRegistration);
        
        when(uploadsRegistration.addResourceLocations("file:src/main/resources/static/uploads/"))
            .thenReturn(uploadsRegistration);
        when(staticRegistration.addResourceLocations("classpath:/static/"))
            .thenReturn(staticRegistration);
        when(h2ConsoleRegistration.addResourceLocations("classpath:/h2-console/"))
            .thenReturn(h2ConsoleRegistration);

        WebConfig webConfig = new WebConfig();
        webConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registry).addResourceHandler("/static/**");
        verify(registry).addResourceHandler("/h2-console/**");
        
        verify(uploadsRegistration).addResourceLocations("file:src/main/resources/static/uploads/");
        verify(staticRegistration).addResourceLocations("classpath:/static/");
        verify(h2ConsoleRegistration).addResourceLocations("classpath:/h2-console/");
    }
}