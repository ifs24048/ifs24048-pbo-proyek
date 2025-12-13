package com.bakery.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bakery.entity.Product;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setUserId(UUID.randomUUID());
        product.setProductName("Roti Tawar");
        product.setDescription("Roti tawar lembut");
        product.setPrice(15000.0);
        product.setStock(10);
        product.setCategory("Roti");
        product.setImageUrl("roti.jpg");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        entityManager.persist(product);
        entityManager.flush();
    }

    @Test
    void testFindAll() {
        Iterable<Product> products = productRepository.findAll();
        assertTrue(products.iterator().hasNext());
    }

    @Test
    void testFindById() {
        Optional<Product> found = productRepository.findById(product.getId());
        assertTrue(found.isPresent());
        assertEquals("Roti Tawar", found.get().getProductName());
    }

    @Test
    void testSaveProduct() {
        Product newProduct = new Product();
        newProduct.setProductName("Donat");
        newProduct.setPrice(5000.0);
        newProduct.setStock(20);
        newProduct.setUserId(UUID.randomUUID());
        newProduct.setCategory("Donat");
        
        
        Product saved = productRepository.save(newProduct);
        assertNotNull(saved.getId());
    }

    @Test
    void testDeleteProduct() {
        productRepository.deleteById(product.getId());
        entityManager.flush(); // Pastikan delete tereksekusi di DB
        
        Optional<Product> found = productRepository.findById(product.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateProduct() throws InterruptedException {
        // 1. Ambil data awal
        Product existingProduct = productRepository.findById(product.getId()).get();
        LocalDateTime oldUpdatedAt = existingProduct.getUpdatedAt();

        // 2. TUNGGU SEBENTAR (PENTING AGAR WAKTU BERUBAH)
        Thread.sleep(50); 

        // 3. Update data
        existingProduct.setProductName("Roti Tawar Spesial");
        existingProduct.setStock(50);
        
        // 4. Save & Flush
        productRepository.save(existingProduct);
        entityManager.flush(); 
        entityManager.clear(); // Bersihkan cache agar data diambil ulang dari DB

        // 5. Verifikasi
        Product updatedProduct = productRepository.findById(product.getId()).get();
        
        assertEquals("Roti Tawar Spesial", updatedProduct.getProductName());
        assertEquals(50, updatedProduct.getStock());
        
        // Assertion ini yang sebelumnya gagal (expected true but was false)
        // Sekarang harusnya berhasil karena ada sleep + clear
        assertTrue(updatedProduct.getUpdatedAt().isAfter(oldUpdatedAt), 
                   "UpdatedAt baru harus lebih besar dari yang lama");
    }
}