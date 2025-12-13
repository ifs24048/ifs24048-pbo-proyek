package com.bakery.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = new Product();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        product.setId(id);
        product.setUserId(userId);
        product.setProductName("Test Product");
        product.setCategory("Kue");
        product.setPrice(10000.0);
        product.setStock(50);
        product.setDescription("Test Description");
        product.setImageUrl("/uploads/test.jpg");
        product.setIsAvailable(true);
        product.setSoldCount(10);
        
        assertEquals(id, product.getId());
        assertEquals(userId, product.getUserId());
        assertEquals("Test Product", product.getProductName());
        assertEquals("Kue", product.getCategory());
        assertEquals(10000.0, product.getPrice());
        assertEquals(50, product.getStock());
        assertEquals("Test Description", product.getDescription());
        assertEquals("/uploads/test.jpg", product.getImageUrl());
        assertTrue(product.getIsAvailable());
        assertEquals(10, product.getSoldCount());
        assertNull(product.getCreatedAt());
        assertNull(product.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        Product product = new Product();
        product.onCreate();
        
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        // Do not assert exact equality for LocalDateTime.now() as it can be flaky
        // asserting non-null is sufficient for this test's purpose.
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        Product product = new Product();
        product.onCreate();
        
        LocalDateTime createdAt = product.getCreatedAt();
        Thread.sleep(10);
        product.onUpdate();
        
        assertNotNull(product.getUpdatedAt());
        assertTrue(product.getUpdatedAt().isAfter(createdAt));
    }
}