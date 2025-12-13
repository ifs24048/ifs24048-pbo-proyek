package com.bakery.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Halaman tidak ditemukan");
                model.addAttribute("errorDescription", "Halaman yang Anda cari tidak ditemukan atau telah dipindahkan.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorMessage", "Akses Ditolak");
                model.addAttribute("errorDescription", "Anda tidak memiliki izin untuk mengakses halaman ini.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Kesalahan Server");
                model.addAttribute("errorDescription", "Terjadi kesalahan pada server. Silakan coba lagi nanti.");
            } else {
                model.addAttribute("errorCode", String.valueOf(statusCode));
                model.addAttribute("errorMessage", "Terjadi Kesalahan");
                model.addAttribute("errorDescription", "Terjadi kesalahan yang tidak terduga.");
            }
        }
        
        return "error";
    }
}