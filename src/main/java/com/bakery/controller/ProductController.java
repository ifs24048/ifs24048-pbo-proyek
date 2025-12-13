package com.bakery.controller;

import com.bakery.entity.Product;
import com.bakery.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");

        model.addAttribute("products", productService.getProductsByUserId(userId));
        return "products/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {

        model.addAttribute("product", new Product());
        return "products/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpSession session) throws IOException {

        UUID userId = (UUID) session.getAttribute("userId");

        if (bindingResult.hasErrors()) {
            return "products/create"; // Return to the form with errors
        }

        if (product.getSoldCount() == null) {
            product.setSoldCount(0);
        }

        Product savedProduct = productService.createProduct(product, userId);

        if (imageFile != null && !imageFile.isEmpty()) {
            productService.updateProductImage(savedProduct.getId(), imageFile);
        }

        return "redirect:/products?success=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model, HttpSession session) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/edit";
        }
        return "redirect:/products";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(
            @PathVariable UUID id,
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "products/edit"; // Return to the edit form with errors
        }

        if (product.getSoldCount() == null) {
            Optional<Product> existingProduct = productService.getProductById(id);
            product.setSoldCount(existingProduct.map(Product::getSoldCount).orElse(0));
        }

        try {
            productService.updateProduct(id, product);
        } catch (RuntimeException e) {
            // Log the error if necessary
            // In this specific case, if "Product not found", we redirect
            if (e.getMessage().equals("Product not found")) {
                return "redirect:/products";
            }
            throw e; // Re-throw other unexpected exceptions
        }
        return "redirect:/products?success=true";
    }

    @GetMapping("/edit-image/{id}")
    public String showEditImageForm(@PathVariable UUID id, Model model, HttpSession session) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/edit-image";
        }
        return "redirect:/products";
    }

    @PostMapping("/update-image/{id}")
    public String updateProductImage(
            @PathVariable UUID id,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session) { // Removed throws IOException

        try {
            productService.updateProductImage(id, imageFile);
        } catch (IOException e) {
            System.err.println("Failed to update product image: " + e.getMessage()); // Log the error
            return "redirect:/products?error=image_upload_failed"; // Redirect with error
        }
        return "redirect:/products?success=true";
    }

    @GetMapping("/confirm-delete/{id}")
    public String showDeleteConfirmation(@PathVariable UUID id, Model model, HttpSession session) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/delete";
        }
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable UUID id, HttpSession session) {

        productService.deleteProduct(id);
        return "redirect:/products?success=true";
    }

    @GetMapping("/detail/{id}")
    public String showProductDetail(@PathVariable UUID id, Model model, HttpSession session) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/detail";
        }
        return "redirect:/products";
    }

    @GetMapping("/update-sales/{id}")
    public String showUpdateSalesForm(@PathVariable UUID id, Model model, HttpSession session) {

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/update-sales";
        }
        return "redirect:/products";
    }

    @PostMapping("/update-sales/{id}")
    public String updateSales(
            @PathVariable UUID id,
            @RequestParam Integer soldCount,
            HttpSession session) {

        try {
            productService.updateSales(id, soldCount);
        } catch (RuntimeException e) {
            // Log the error if necessary
            if (e.getMessage().equals("Product not found")) {
                return "redirect:/products";
            }
            throw e; // Re-throw other unexpected exceptions
        }

        return "redirect:/products?success=true";
    }
}