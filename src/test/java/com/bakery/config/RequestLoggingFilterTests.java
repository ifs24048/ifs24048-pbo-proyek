package com.bakery.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void testDoFilter() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(response.getStatus()).thenReturn(200);

        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithException() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(500);

        doThrow(new RuntimeException("Test exception"))
                .when(filterChain).doFilter(request, response);

        RequestLoggingFilter filter = new RequestLoggingFilter();

        try {
            filter.doFilter(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testInitAndDestroy() throws jakarta.servlet.ServletException {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.init(null);
        filter.destroy();
        // Assertions are that no exception is thrown
    }

    @Test
    void testDoFilter_NonHttpRequest() throws Exception {
        jakarta.servlet.ServletRequest nonHttpRequest = mock(jakarta.servlet.ServletRequest.class);

        RequestLoggingFilter filter = new RequestLoggingFilter();

        try {
            filter.doFilter(nonHttpRequest, response, filterChain);
        } catch (ClassCastException e) {
            // Expected
        }
    }
}