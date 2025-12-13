package com.bakery.controller;

import com.bakery.entity.Product;
import com.bakery.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ChartController {

    private final ProductService productService;

    public ChartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Product> userProducts = productService.getProductsByUserId(userId);

        // Statistics
        long totalProducts = userProducts.size();
        long availableProducts = userProducts.stream()
                .filter(p -> p.getIsAvailable() != null && p.getIsAvailable())
                .count();
        long lowStockProducts = userProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 10)
                .count();
        int totalSold = userProducts.stream()
                .mapToInt(p -> p.getSoldCount() != null ? p.getSoldCount() : 0)
                .sum();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("availableProducts", availableProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("totalSold", totalSold);

        // ========== HANYA 2 CHART DATA ==========

        // 1. Best Selling Products (Top 5)
        List<Product> bestSelling = userProducts.stream()
                .sorted((p1, p2) -> {
                    Integer sold1 = p1.getSoldCount() != null ? p1.getSoldCount() : 0;
                    Integer sold2 = p2.getSoldCount() != null ? p2.getSoldCount() : 0;
                    return sold2.compareTo(sold1);
                })
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("bestSellingProducts", bestSelling);
        model.addAttribute("bestSellingLabels",
                bestSelling.stream().map(Product::getProductName).collect(Collectors.toList()));
        model.addAttribute("bestSellingData",
                bestSelling.stream()
                        .map(p -> p.getSoldCount() != null ? p.getSoldCount() : 0)
                        .collect(Collectors.toList()));

        // 2. Low Stock Products (Stok ≤ 10)
        // 2. Low Stock Products (Stok <= 10)
        List<Product> lowStock = userProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 10)
                .sorted(Comparator.comparing(Product::getStock))
                .collect(Collectors.toList());

        model.addAttribute("lowStockProductsList", lowStock);
        model.addAttribute("lowStockLabels",
                lowStock.stream().map(Product::getProductName).collect(Collectors.toList()));
        model.addAttribute("lowStockData",
                lowStock.stream()
                        .map(Product::getStock)
                        .collect(Collectors.toList()));

        return "dashboard";
    }
}