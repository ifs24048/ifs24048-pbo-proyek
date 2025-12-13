package com.bakery.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.bakery.entity.Product;
import com.bakery.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile imageFile;

    private ProductService productService;
    private UUID userId;
    private UUID productId;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, fileStorageService);
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setProductName("Test Product");
        mockProduct.setPrice(10000.0);
        mockProduct.setStock(50);
        mockProduct.setUserId(userId);
    }

    @Test
    void testGetProductsByUserId() {
        List<Product> expectedProducts = Arrays.asList(mockProduct);

        when(productRepository.findByUserId(userId)).thenReturn(expectedProducts);

        List<Product> result = productService.getProductsByUserId(userId);

        assertEquals(expectedProducts, result);
        verify(productRepository).findByUserId(userId);
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.getProductById(productId);

        assertTrue(result.isPresent());
        assertEquals(mockProduct, result.get());
        verify(productRepository).findById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(productId);

        assertFalse(result.isPresent());
        verify(productRepository).findById(productId);
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product result = productService.createProduct(mockProduct, userId);

        assertEquals(mockProduct, result);
        assertEquals(userId, result.getUserId());
        verify(productRepository).save(mockProduct);
    }

    @Test
    void testUpdateProduct_Success() {
        Product updatedProduct = new Product();
        updatedProduct.setProductName("Updated Product");
        updatedProduct.setPrice(15000.0);
        updatedProduct.setStock(30);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product result = productService.updateProduct(productId, updatedProduct);

        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(mockProduct);
    }

    @Test
    void testUpdateProduct_NotFound() {
        Product updatedProduct = new Product();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(productId, updatedProduct));

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        verify(productRepository).deleteById(productId);
    }

    @Test
    void testUpdateProductImage_Success() throws IOException {
        String fileName = "test.jpg";
        String storedFileName = "/uploads/" + UUID.randomUUID() + "_" + fileName;

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(imageFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(imageFile)).thenReturn(storedFileName);

        productService.updateProductImage(productId, imageFile);

        verify(productRepository).findById(productId);
        verify(fileStorageService).storeFile(imageFile);
        verify(productRepository).save(mockProduct);
        assertEquals(storedFileName, mockProduct.getImageUrl());
    }

    @Test
    void testUpdateProductImage_EmptyFile() throws IOException {
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(imageFile.isEmpty()).thenReturn(true);

        productService.updateProductImage(productId, imageFile);

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void testUpdateProductImage_NullFile() throws IOException {
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        productService.updateProductImage(productId, null);

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void testFindByCategory() {
        String category = "Kue";
        List<Product> expectedProducts = Arrays.asList(mockProduct);

        when(productRepository.findByCategory(category)).thenReturn(expectedProducts);

        List<Product> result = productService.findByCategory(category);

        assertEquals(expectedProducts, result);
        verify(productRepository).findByCategory(category);
    }

    @Test
    void testFindByIsAvailable() {
        Boolean isAvailable = true;
        List<Product> expectedProducts = Arrays.asList(mockProduct);

        when(productRepository.findByIsAvailable(isAvailable)).thenReturn(expectedProducts);

        List<Product> result = productService.findByIsAvailable(isAvailable);

        assertEquals(expectedProducts, result);
        verify(productRepository).findByIsAvailable(isAvailable);
    }

    @Test
    void testFindByStockLessThan() {
        Integer stock = 10;
        List<Product> expectedProducts = Arrays.asList(mockProduct);

        when(productRepository.findByStockLessThan(stock)).thenReturn(expectedProducts);

        List<Product> result = productService.findByStockLessThan(stock);

        assertEquals(expectedProducts, result);
        verify(productRepository).findByStockLessThan(stock);
    }

    @Test
    void testUpdateProductImage_ProductNotFound() throws IOException {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.updateProductImage(productId, imageFile));

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateProductImage_WithExistingImage() throws IOException {
        String oldImageUrl = "/uploads/old-image.jpg";
        String newImageUrl = "/uploads/new-image.jpg";

        mockProduct.setImageUrl(oldImageUrl);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(imageFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(imageFile)).thenReturn(newImageUrl);

        productService.updateProductImage(productId, imageFile);

        verify(productRepository).findById(productId);
        verify(fileStorageService).deleteFile(oldImageUrl); // Verify deletion of old file
        verify(fileStorageService).storeFile(imageFile);
        verify(productRepository).save(mockProduct);
        assertEquals(newImageUrl, mockProduct.getImageUrl());
    }

    @Test
    void testUpdateSales_Success() {
        int soldCount = 50;
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        productService.updateSales(productId, soldCount);

        verify(productRepository).findById(productId);
        verify(productRepository).save(mockProduct);
        assertEquals(soldCount, mockProduct.getSoldCount());
    }

    @Test
    void testUpdateSales_ProductNotFound() {
        int soldCount = 50;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.updateSales(productId, soldCount));

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }
}