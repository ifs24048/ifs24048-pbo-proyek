package com.bakery.service;

import com.bakery.entity.Product;
import com.bakery.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> getProductsByUserId(UUID userId) {
        return productRepository.findByUserId(userId);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product, UUID userId) {
        product.setUserId(userId);
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existingProduct.setProductName(product.getProductName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setIsAvailable(product.getIsAvailable());
        existingProduct.setSoldCount(product.getSoldCount());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    public void updateProductImage(UUID id, MultipartFile imageFile) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (imageFile != null && !imageFile.isEmpty()) {
            if (product.getImageUrl() != null) {
                fileStorageService.deleteFile(product.getImageUrl());
            }

            String storageFileName = fileStorageService.storeFile(imageFile);
            product.setImageUrl(storageFileName);
            productRepository.save(product);
        }
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findByIsAvailable(Boolean isAvailable) {
        return productRepository.findByIsAvailable(isAvailable);
    }

    public List<Product> findByStockLessThan(Integer stock) {
        return productRepository.findByStockLessThan(stock);
    }

    public void updateSales(UUID productId, int soldCount) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        existingProduct.setSoldCount(soldCount);
        productRepository.save(existingProduct);
    }
}