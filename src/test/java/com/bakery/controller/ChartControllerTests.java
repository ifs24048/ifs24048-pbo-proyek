package com.bakery.controller;

import com.bakery.entity.Product;
import com.bakery.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    private ChartController chartController;
    private UUID userId;
    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        chartController = new ChartController(productService);
        userId = UUID.randomUUID();

        mockProducts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setProductName("Product " + i);
            product.setPrice(1000.0 * i);
            product.setStock(i * 5);
            product.setSoldCount(i * 2);
            product.setIsAvailable(true);
            mockProducts.add(product);
        }
    }

    @Test
    void testShowDashboard_NoSession_RedirectsToLogin() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("redirect:/login", viewName);
        verify(productService, never()).getProductsByUserId(any());
    }

    @Test
    void testShowDashboard_WithSession() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(productService.getProductsByUserId(userId)).thenReturn(mockProducts);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);

        // Verify statistics
        verify(model).addAttribute("totalProducts", 10L);
        verify(model).addAttribute("availableProducts", 10L);
        verify(model).addAttribute(eq("totalSold"), anyInt());

        // Verify best selling products
        List<Product> bestSelling = mockProducts.stream()
                .sorted((p1, p2) -> Integer.compare(
                        p2.getSoldCount() != null ? p2.getSoldCount() : 0,
                        p1.getSoldCount() != null ? p1.getSoldCount() : 0))
                .limit(5)
                .collect(Collectors.toList());

        verify(model).addAttribute("bestSellingProducts", bestSelling);

        // Verify low stock products
        List<Product> lowStock = mockProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 10)
                .collect(Collectors.toList());

        verify(model).addAttribute("lowStockProductsList", lowStock);
    }

    @Test
    void testShowDashboard_EmptyProducts() {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(productService.getProductsByUserId(userId)).thenReturn(Collections.emptyList());

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("totalProducts", 0L);
        verify(model).addAttribute("availableProducts", 0L);
        verify(model).addAttribute("lowStockProducts", 0L);
        verify(model).addAttribute("totalSold", 0);

        verify(model).addAttribute(eq("bestSellingProducts"), any(List.class));
        verify(model).addAttribute(eq("lowStockProductsList"), any(List.class));
    }

    @Test
    void testShowDashboard_WithNullValues() {
        when(session.getAttribute("userId")).thenReturn(userId);

        List<Product> productsWithNulls = new ArrayList<>();
        Product product1 = new Product();
        product1.setProductName("Product 1");
        product1.setStock(null);
        product1.setSoldCount(null);
        product1.setIsAvailable(null);
        productsWithNulls.add(product1);

        when(productService.getProductsByUserId(userId)).thenReturn(productsWithNulls);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("totalProducts", 1L);
        verify(model).addAttribute("availableProducts", 0L); // null considered as not available
        verify(model).addAttribute("lowStockProducts", 0L); // null stock not considered low
        verify(model).addAttribute("totalSold", 0);
    }

    @Test
    void testShowDashboard_WithMixedNullSoldCounts() {
        when(session.getAttribute("userId")).thenReturn(userId);

        List<Product> products = new ArrayList<>();

        // Product with null soldCount
        Product product1 = new Product();
        product1.setProductName("Product 1");
        product1.setSoldCount(null);
        product1.setStock(20);
        product1.setIsAvailable(true);
        products.add(product1);

        // Product with non-null soldCount
        Product product2 = new Product();
        product2.setProductName("Product 2");
        product2.setSoldCount(10);
        product2.setStock(20);
        product2.setIsAvailable(true);
        products.add(product2);

        when(productService.getProductsByUserId(userId)).thenReturn(products);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("totalProducts", 2L);
        verify(model).addAttribute("totalSold", 10); // Only product2's soldCount
    }

    @Test
    void testShowDashboard_WithMixedNullStocks() {
        when(session.getAttribute("userId")).thenReturn(userId);

        List<Product> products = new ArrayList<>();

        // Product with null stock
        Product product1 = new Product();
        product1.setProductName("Product 1");
        product1.setSoldCount(5);
        product1.setStock(null);
        product1.setIsAvailable(true);
        products.add(product1);

        // Product with low stock
        Product product2 = new Product();
        product2.setProductName("Product 2");
        product2.setSoldCount(10);
        product2.setStock(5); // Low stock <= 10
        product2.setIsAvailable(true);
        products.add(product2);

        when(productService.getProductsByUserId(userId)).thenReturn(products);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("lowStockProducts", 1L); // Only product2 has low stock
    }

    @Test
    void testShowDashboard_CoverageAllBranches() {
        when(session.getAttribute("userId")).thenReturn(userId);

        List<Product> products = new ArrayList<>();

        // 1. Available=False | Stock=20 | Sold=Null
        Product p1 = new Product();
        p1.setProductName("P1");
        p1.setIsAvailable(false);
        p1.setStock(20);
        p1.setSoldCount(null);
        products.add(p1);

        // 2. Available=True | Stock=10 (Boundary Low) | Sold=5
        Product p2 = new Product();
        p2.setProductName("P2");
        p2.setIsAvailable(true);
        p2.setStock(10);
        p2.setSoldCount(5);
        products.add(p2);

        // 3. Available=True | Stock=11 (Boundary High) | Sold=10
        Product p3 = new Product();
        p3.setProductName("P3");
        p3.setIsAvailable(true);
        p3.setStock(11);
        p3.setSoldCount(10);
        products.add(p3);

        // 4. Available=Null | Stock=Null | Sold=Null
        Product p4 = new Product();
        p4.setProductName("P4");
        p4.setIsAvailable(null);
        p4.setStock(null);
        p4.setSoldCount(null);
        products.add(p4);

        // 5. Available=True | Stock=5 | Sold=Null
        Product p5 = new Product();
        p5.setProductName("P5");
        p5.setIsAvailable(true);
        p5.setStock(5);
        p5.setSoldCount(null);
        products.add(p5);

        when(productService.getProductsByUserId(userId)).thenReturn(products);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);

        // Assertions to ensure calculation logic runs
        verify(model).addAttribute(eq("totalProducts"), eq(5L));
        // Available: P2, P3, P5 (P1 false, P4 null) -> 3
        verify(model).addAttribute(eq("availableProducts"), eq(3L));
        // Low Stock (<=10): P2(10), P5(5) (P1 20, P3 11, P4 null) -> 2
        verify(model).addAttribute(eq("lowStockProducts"), eq(2L));
        // Total Sold: 5 + 10 = 15
        verify(model).addAttribute(eq("totalSold"), eq(15));
    }

    @Test
    void testShowDashboard_DetailedComparatorAndFilterCoverage() {
        when(session.getAttribute("userId")).thenReturn(userId);

        List<Product> products = new ArrayList<>();

        // 1. Base Product
        Product p1 = new Product();
        p1.setProductName("P1");
        p1.setSoldCount(10);
        p1.setStock(5);
        p1.setIsAvailable(true);
        products.add(p1);

        // 2. Exact Duplicate Values for Comparator EQUALITY Check (Sold=10, Stock=5)
        Product p2 = new Product();
        p2.setProductName("P2");
        p2.setSoldCount(10);
        p2.setStock(5);
        p2.setIsAvailable(true);
        products.add(p2);

        // 3. Different Sold, Same Stock (Sold=5, Stock=5) - Tests Stock Equality
        Product p3 = new Product();
        p3.setProductName("P3");
        p3.setSoldCount(5);
        p3.setStock(5);
        p3.setIsAvailable(true);
        products.add(p3);

        // 4. Null Values - Tests Null Handling in Comparators/Maps
        Product p4 = new Product();
        p4.setProductName("P4");
        p4.setSoldCount(null);
        p4.setStock(null);
        p4.setIsAvailable(true);
        products.add(p4);

        // 5. Zero Values
        Product p5 = new Product();
        p5.setProductName("P5");
        p5.setSoldCount(0);
        p5.setStock(0);
        p5.setIsAvailable(true);
        products.add(p5);

        // 6. High Stock (Not Low Stock)
        Product p6 = new Product();
        p6.setProductName("P6");
        p6.setSoldCount(20);
        p6.setStock(20);
        p6.setIsAvailable(true);
        products.add(p6);

        when(productService.getProductsByUserId(userId)).thenReturn(products);

        String viewName = chartController.showDashboard(model, session);

        assertEquals("dashboard", viewName);

        // Verify the logic ran without exceptions and attributes are present
        verify(model).addAttribute(eq("bestSellingProducts"), any(List.class));
        verify(model).addAttribute(eq("lowStockProductsList"), any(List.class));
    }
}
