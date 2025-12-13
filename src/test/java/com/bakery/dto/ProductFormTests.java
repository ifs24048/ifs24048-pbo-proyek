package com.bakery.dto;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProductFormTest {

    @Test
    void testProductForm() {
        ProductForm form = new ProductForm();
        MultipartFile mockFile = mock(MultipartFile.class);
        
        form.setProductName("Test Product");
        form.setCategory("Kue");
        form.setPrice(10000.0);
        form.setStock(50);
        form.setDescription("Test Description");
        form.setImageFile(mockFile);
        form.setIsAvailable(true);
        form.setSoldCount(10);
        
        assertEquals("Test Product", form.getProductName());
        assertEquals("Kue", form.getCategory());
        assertEquals(10000.0, form.getPrice());
        assertEquals(50, form.getStock());
        assertEquals("Test Description", form.getDescription());
        assertEquals(mockFile, form.getImageFile());
        assertTrue(form.getIsAvailable());
        assertEquals(10, form.getSoldCount());
    }
}