package com.bakery.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @Mock
    private HttpServletRequest request;


    @Mock
    private Model model;

    @Mock
    private CustomErrorController controller; // Tidak perlu di-mock sebenarnya, tapi ok

    @Test
    void testHandleError_404() {
        CustomErrorController realController = new CustomErrorController();
        
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("404");
        
        String viewName = realController.handleError(request, model);
        
        assertEquals("error", viewName);
        // KEMBALI KE STRING: Karena Controller Anda mengirim String
        verify(model).addAttribute(eq("errorCode"), eq("404"));
        verify(model).addAttribute(eq("errorMessage"), eq("Halaman tidak ditemukan"));
    }

    @Test
    void testHandleError_403() {
        CustomErrorController realController = new CustomErrorController();
        
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("403");
        
        String viewName = realController.handleError(request, model);
        
        assertEquals("error", viewName);
        // KEMBALI KE STRING
        verify(model).addAttribute(eq("errorCode"), eq("403"));
        verify(model).addAttribute(eq("errorMessage"), eq("Akses Ditolak"));
    }

    @Test
    void testHandleError_500() {
        CustomErrorController realController = new CustomErrorController();
        
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("500");
        
        String viewName = realController.handleError(request, model);
        
        assertEquals("error", viewName);
        // KEMBALI KE STRING
        verify(model).addAttribute(eq("errorCode"), eq("500"));
        verify(model).addAttribute(eq("errorMessage"), eq("Kesalahan Server"));
    }

    @Test
    void testHandleError_GenericError() {
        CustomErrorController realController = new CustomErrorController();
        
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn("400");
        
        String viewName = realController.handleError(request, model);
        
        assertEquals("error", viewName);
        // KEMBALI KE STRING
        verify(model).addAttribute(eq("errorCode"), eq("400"));
        verify(model).addAttribute(eq("errorMessage"), eq("Terjadi Kesalahan"));
    }

    @Test
    void testHandleError_NullStatus() {
        CustomErrorController realController = new CustomErrorController();
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);
        
        String viewName = realController.handleError(request, model);
        assertEquals("error", viewName);
        verify(model, never()).addAttribute(eq("errorCode"), any());
    }
}