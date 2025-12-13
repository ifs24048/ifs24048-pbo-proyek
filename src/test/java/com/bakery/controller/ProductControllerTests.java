package com.bakery.controller;

import com.bakery.entity.Product;
import com.bakery.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import jakarta.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ProductService productService;

        @Test
        void testListProducts_unauthenticated() throws Exception {
                mockMvc.perform(get("/products"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testListProducts_authenticated() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                List<Product> products = new ArrayList<>();
                Product productForList = new Product();
                productForList.setSoldCount(0); // Set soldCount to avoid null pointer in Thymeleaf aggregation
                products.add(productForList);
                when(productService.getProductsByUserId(userId)).thenReturn(products);

                mockMvc.perform(get("/products").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/list"))
                                .andExpect(model().attributeExists("products"));

                verify(productService, times(1)).getProductsByUserId(userId);
        }

        @Test
        void testShowCreateForm_unauthenticated() throws Exception {
                mockMvc.perform(get("/products/create"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowCreateForm_authenticated() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                mockMvc.perform(get("/products/create").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/create"))
                                .andExpect(model().attributeExists("product"));
        }

        @Test
        void testCreateProduct_success() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                Product product = new Product();
                product.setId(UUID.randomUUID()); // Set a UUID for the product
                product.setProductName("Test Product");
                product.setCategory("Test Category");
                product.setPrice(10.0);
                product.setStock(100);
                product.setDescription("Test Description");

                MockMultipartFile imageFile = new MockMultipartFile(
                                "imageFile",
                                "test-image.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "some-image-content".getBytes());

                when(productService.createProduct(any(Product.class), any(UUID.class))).thenReturn(product);

                mockMvc.perform(multipart("/products/create")
                                .file(imageFile)
                                .session(session)
                                .param("productName", product.getProductName())
                                .param("category", product.getCategory())
                                .param("price", product.getPrice().toString())
                                .param("stock", product.getStock().toString())
                                .param("description", product.getDescription()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).createProduct(any(Product.class), any(UUID.class));
                verify(productService, times(1)).updateProductImage(any(UUID.class), any(MockMultipartFile.class));
        }

        @Test
        void testCreateProduct_invalidData() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                // Missing productName and category, which are required by @NotBlank
                mockMvc.perform(multipart("/products/create")
                                .session(session)
                                .param("price", "10.0")
                                .param("stock", "100")
                                .param("description", "Test Description"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/create"))
                                .andExpect(model().attributeHasFieldErrors("product", "productName"))
                                .andExpect(model().attributeHasFieldErrors("product", "category"));

                verify(productService, never()).createProduct(any(Product.class), any(UUID.class));
        }

        @Test
        void testShowEditForm_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(get("/products/edit/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowEditForm_authenticatedAndFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product product = new Product();
                product.setId(productId);
                when(productService.getProductById(productId)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/products/edit/{id}", productId).session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/edit"))
                                .andExpect(model().attributeExists("product"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testShowEditForm_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                when(productService.getProductById(productId)).thenReturn(Optional.empty());

                mockMvc.perform(get("/products/edit/{id}", productId).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testUpdateProduct_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(post("/products/update/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testUpdateProduct_authenticatedAndSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);

                when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(productToUpdate);

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).updateProduct(eq(productId), any(Product.class));
        }

        @Test
        void testUpdateProduct_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);

                when(productService.updateProduct(eq(productId), any(Product.class)))
                                .thenThrow(new RuntimeException("Product not found"));

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString()))
                                .andExpect(status().is3xxRedirection()) // Controller now redirects
                                .andExpect(redirectedUrl("/products")); // Expect redirect to /products

                verify(productService, times(1)).updateProduct(eq(productId), any(Product.class));
        }

        @Test
        void testUpdateProduct_authenticatedAndSoldCountNull() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product existingProduct = new Product();
                existingProduct.setId(productId);
                existingProduct.setProductName("Existing Name");
                existingProduct.setCategory("Existing Category");
                existingProduct.setPrice(10.0);
                existingProduct.setStock(100);
                existingProduct.setSoldCount(20); // Existing sold count

                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);
                // soldCount is intentionally null in productToUpdate, so not passed as param

                when(productService.getProductById(productId)).thenReturn(Optional.of(existingProduct));
                when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(existingProduct);

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).getProductById(productId); // verify soldCount retrieval
                verify(productService, times(1)).updateProduct(eq(productId), argThat(
                                p -> p.getSoldCount() != null && p.getSoldCount().equals(existingProduct.getSoldCount())
                                                && // Ensure
                                                   // original
                                                   // soldCount
                                                   // is
                                                   // preserved
                                                p.getProductName().equals(productToUpdate.getProductName()) // and other
                                                                                                            // fields
                                                                                                            // are
                                                                                                            // updated
                ));
        }

        @Test
        void testShowEditImageForm_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(get("/products/edit-image/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowEditImageForm_authenticatedAndFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product product = new Product();
                product.setId(productId);
                when(productService.getProductById(productId)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/products/edit-image/{id}", productId).session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/edit-image"))
                                .andExpect(model().attributeExists("product"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testShowEditImageForm_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                when(productService.getProductById(productId)).thenReturn(Optional.empty());

                mockMvc.perform(get("/products/edit-image/{id}", productId).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testUpdateProductImage_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(post("/products/update-image/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testUpdateProductImage_authenticatedAndSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                MockMultipartFile imageFile = new MockMultipartFile(
                                "imageFile",
                                "test-image.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "some-image-content".getBytes());

                doNothing().when(productService).updateProductImage(eq(productId), any(MultipartFile.class));

                mockMvc.perform(multipart("/products/update-image/{id}", productId)
                                .file(imageFile)
                                .session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).updateProductImage(eq(productId), any(MultipartFile.class));
        }

        @Test
        void testShowDeleteConfirmation_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(get("/products/confirm-delete/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowDeleteConfirmation_authenticatedAndFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product product = new Product();
                product.setId(productId);
                when(productService.getProductById(productId)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/products/confirm-delete/{id}", productId).session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/delete"))
                                .andExpect(model().attributeExists("product"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testDeleteProduct_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(post("/products/delete/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowProductDetail_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(get("/products/detail/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowProductDetail_authenticatedAndFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product product = new Product();
                product.setId(productId);
                when(productService.getProductById(productId)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/products/detail/{id}", productId).session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/detail"))
                                .andExpect(model().attributeExists("product"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testShowUpdateSalesForm_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(get("/products/update-sales/{id}", productId))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testShowUpdateSalesForm_authenticatedAndFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product product = new Product();
                product.setId(productId);
                when(productService.getProductById(productId)).thenReturn(Optional.of(product));

                mockMvc.perform(get("/products/update-sales/{id}", productId).session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/update-sales"))
                                .andExpect(model().attributeExists("product"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testUpdateSales_unauthenticated() throws Exception {
                UUID productId = UUID.randomUUID();
                mockMvc.perform(post("/products/update-sales/{id}", productId)
                                .param("soldCount", "10"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testUpdateSales_authenticatedAndSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                int soldCount = 10;

                doNothing().when(productService).updateSales(eq(productId), eq(soldCount));

                mockMvc.perform(post("/products/update-sales/{id}", productId)
                                .session(session)
                                .param("soldCount", String.valueOf(soldCount)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).updateSales(eq(productId), eq(soldCount));
        }

        @Test
        void testUpdateProduct_authenticatedAndOtherException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);

                when(productService.updateProduct(eq(productId), any(Product.class)))
                                .thenThrow(new RuntimeException("Some other error"));

                // Expect ServletException wrapping the RuntimeException
                ServletException exception = assertThrows(ServletException.class, () -> {
                        mockMvc.perform(post("/products/update/{id}", productId)
                                        .session(session)
                                        .param("productName", productToUpdate.getProductName())
                                        .param("category", productToUpdate.getCategory())
                                        .param("price", productToUpdate.getPrice().toString())
                                        .param("stock", productToUpdate.getStock().toString()));
                });

                assertNotNull(exception.getCause());
                assertTrue(exception.getCause() instanceof RuntimeException);
                assertEquals("Some other error", exception.getCause().getMessage());

                verify(productService, times(1)).updateProduct(eq(productId),
                                any(Product.class));
        }

        @Test
        void testUpdateProductImage_authenticatedAndOtherException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                MockMultipartFile imageFile = new MockMultipartFile(
                                "imageFile",
                                "test-image.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "some-image-content".getBytes());

                doThrow(new RuntimeException("Some other image error")).when(productService).updateProductImage(
                                eq(productId),
                                any(MultipartFile.class));

                try {
                        mockMvc.perform(multipart("/products/update-image/{id}", productId)
                                        .file(imageFile)
                                        .session(session));
                } catch (Exception e) {
                        // RuntimeException should propagate through
                        assertTrue(e.getCause() instanceof RuntimeException);
                        assertEquals("Some other image error", e.getCause().getMessage());
                }

                verify(productService, times(1)).updateProductImage(eq(productId),
                                any(MultipartFile.class));
        }

        @Test
        void testUpdateSales_authenticatedAndOtherException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                int soldCount = 10;

                doThrow(new RuntimeException("Some other sales error")).when(productService).updateSales(
                                eq(productId),
                                eq(soldCount));

                // Expect ServletException wrapping the RuntimeException
                ServletException exception = assertThrows(ServletException.class, () -> {
                        mockMvc.perform(post("/products/update-sales/{id}", productId)
                                        .session(session)
                                        .param("soldCount", String.valueOf(soldCount)));
                });

                assertNotNull(exception.getCause());
                assertTrue(exception.getCause() instanceof RuntimeException);
                assertEquals("Some other sales error", exception.getCause().getMessage());

                verify(productService, times(1)).updateSales(eq(productId), eq(soldCount));
        }

        @Test
        void testCreateProduct_unauthenticated() throws Exception {
                mockMvc.perform(multipart("/products/create")
                                .param("productName", "Test Product")
                                .param("category", "Test Category")
                                .param("price", "10.0")
                                .param("stock", "100"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));

                verify(productService, never()).createProduct(any(Product.class), any(UUID.class));
        }

        @Test
        void testCreateProduct_withoutImageFile() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                Product product = new Product();
                product.setId(UUID.randomUUID());
                product.setProductName("Test Product");
                product.setCategory("Test Category");
                product.setPrice(10.0);
                product.setStock(100);
                product.setDescription("Test Description");

                when(productService.createProduct(any(Product.class), any(UUID.class))).thenReturn(product);

                mockMvc.perform(multipart("/products/create")
                                .session(session)
                                .param("productName", product.getProductName())
                                .param("category", product.getCategory())
                                .param("price", product.getPrice().toString())
                                .param("stock", product.getStock().toString())
                                .param("description", product.getDescription()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).createProduct(any(Product.class), any(UUID.class));
                verify(productService, never()).updateProductImage(any(UUID.class), any(MultipartFile.class));
        }

        @Test
        void testCreateProduct_withNullSoldCount() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                Product product = new Product();
                product.setId(UUID.randomUUID());
                product.setProductName("Test Product");
                product.setCategory("Test Category");
                product.setPrice(10.0);
                product.setStock(100);
                // soldCount is null

                when(productService.createProduct(any(Product.class), any(UUID.class))).thenReturn(product);

                mockMvc.perform(multipart("/products/create")
                                .session(session)
                                .param("productName", product.getProductName())
                                .param("category", product.getCategory())
                                .param("price", product.getPrice().toString())
                                .param("stock", product.getStock().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1))
                                .createProduct(argThat(p -> p.getSoldCount() != null && p.getSoldCount().equals(0)),
                                                any(UUID.class));
        }

        @Test
        void testUpdateProduct_authenticatedAndValidationErrors() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();

                // Missing required fields (productName and category)
                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("price", "10.0")
                                .param("stock", "100"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("products/edit"))
                                .andExpect(model().attributeHasFieldErrors("product", "productName"))
                                .andExpect(model().attributeHasFieldErrors("product", "category"));

                verify(productService, never()).updateProduct(any(UUID.class), any(Product.class));
        }

        @Test
        void testUpdateProductImage_authenticatedAndIOException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                MockMultipartFile imageFile = new MockMultipartFile(
                                "imageFile",
                                "test-image.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "some-image-content".getBytes());

                doThrow(new IOException("File upload failed")).when(productService).updateProductImage(eq(productId),
                                any(MultipartFile.class));

                mockMvc.perform(multipart("/products/update-image/{id}", productId)
                                .file(imageFile)
                                .session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?error=image_upload_failed"));

                verify(productService, times(1)).updateProductImage(eq(productId), any(MultipartFile.class));
        }

        @Test
        void testDeleteProduct_authenticatedAndSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();

                doNothing().when(productService).deleteProduct(productId);

                mockMvc.perform(post("/products/delete/{id}", productId)
                                .session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).deleteProduct(productId);
        }

        @Test
        void testShowProductDetail_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                when(productService.getProductById(productId)).thenReturn(Optional.empty());

                mockMvc.perform(get("/products/detail/{id}", productId).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testShowUpdateSalesForm_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                when(productService.getProductById(productId)).thenReturn(Optional.empty());

                mockMvc.perform(get("/products/update-sales/{id}", productId).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testUpdateSales_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                int soldCount = 10;

                doThrow(new RuntimeException("Product not found")).when(productService).updateSales(eq(productId),
                                eq(soldCount));

                mockMvc.perform(post("/products/update-sales/{id}", productId)
                                .session(session)
                                .param("soldCount", String.valueOf(soldCount)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).updateSales(eq(productId), eq(soldCount));
        }

        @Test
        void testShowDeleteConfirmation_authenticatedAndNotFound() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                when(productService.getProductById(productId)).thenReturn(Optional.empty());

                mockMvc.perform(get("/products/confirm-delete/{id}", productId).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testUpdateProduct_withNonNullSoldCount() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);
                productToUpdate.setSoldCount(25); // Explicitly set soldCount

                when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(productToUpdate);

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString())
                                .param("soldCount", productToUpdate.getSoldCount().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).updateProduct(eq(productId), any(Product.class));
        }

        @Test
        void testCreateProduct_withEmptyImageFile() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                Product product = new Product();
                product.setId(UUID.randomUUID());
                product.setProductName("Test Product");
                product.setCategory("Test Category");
                product.setPrice(10.0);
                product.setStock(100);
                product.setDescription("Test Description");

                // Empty image file (not null, but empty content)
                MockMultipartFile emptyImageFile = new MockMultipartFile(
                                "imageFile",
                                "", // Empty filename
                                MediaType.IMAGE_JPEG_VALUE,
                                new byte[0]); // Empty content

                when(productService.createProduct(any(Product.class), any(UUID.class))).thenReturn(product);

                mockMvc.perform(multipart("/products/create")
                                .file(emptyImageFile)
                                .session(session)
                                .param("productName", product.getProductName())
                                .param("category", product.getCategory())
                                .param("price", product.getPrice().toString())
                                .param("stock", product.getStock().toString())
                                .param("description", product.getDescription()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).createProduct(any(Product.class), any(UUID.class));
                // updateProductImage should NOT be called because file is empty
                verify(productService, never()).updateProductImage(any(UUID.class), any(MultipartFile.class));
        }

        @Test
        void testUpdateProduct_soldCountNullAndNoExistingProduct() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);
                // soldCount is null

                // Existing product not found - should use orElse(0)
                when(productService.getProductById(productId)).thenReturn(Optional.empty());
                when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(productToUpdate);

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).getProductById(productId);
                verify(productService, times(1)).updateProduct(eq(productId),
                                argThat(p -> p.getSoldCount() != null && p.getSoldCount().equals(0)));
        }

        @Test
        void testUpdateProduct_soldCountNullAndExistingProductHasNullSoldCount() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product existingProduct = new Product();
                existingProduct.setId(productId);
                existingProduct.setSoldCount(null); // Existing product has null soldCount

                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);
                // soldCount is null

                when(productService.getProductById(productId)).thenReturn(Optional.of(existingProduct));
                when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(productToUpdate);

                mockMvc.perform(post("/products/update/{id}", productId)
                                .session(session)
                                .param("productName", productToUpdate.getProductName())
                                .param("category", productToUpdate.getCategory())
                                .param("price", productToUpdate.getPrice().toString())
                                .param("stock", productToUpdate.getStock().toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).getProductById(productId);
        }

        @Test
        void testCreateProduct_soldCountExplicitlySet() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                Product product = new Product();
                product.setId(UUID.randomUUID());
                product.setProductName("Test Product");
                product.setCategory("Test Category");
                product.setPrice(10.0);
                product.setStock(100);

                when(productService.createProduct(any(Product.class), any(UUID.class))).thenReturn(product);

                mockMvc.perform(multipart("/products/create")
                                .session(session)
                                .param("productName", product.getProductName())
                                .param("category", product.getCategory())
                                .param("price", product.getPrice().toString())
                                .param("stock", product.getStock().toString())
                                .param("soldCount", "5")) // Explicitly set soldCount
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).createProduct(any(Product.class), any(UUID.class));
        }

        @Test
        void testUpdateProduct_otherException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                Product productToUpdate = new Product();
                productToUpdate.setProductName("Updated Name");
                productToUpdate.setCategory("Updated Category");
                productToUpdate.setPrice(15.0);
                productToUpdate.setStock(50);
                productToUpdate.setSoldCount(10);

                when(productService.updateProduct(eq(productId), any(Product.class)))
                                .thenThrow(new RuntimeException("Database error"));

                try {
                        mockMvc.perform(post("/products/update/{id}", productId)
                                        .session(session)
                                        .param("productName", productToUpdate.getProductName())
                                        .param("category", productToUpdate.getCategory())
                                        .param("price", productToUpdate.getPrice().toString())
                                        .param("stock", productToUpdate.getStock().toString())
                                        .param("soldCount", productToUpdate.getSoldCount().toString()));
                } catch (Exception e) {
                        // Expected - exception should be re-thrown
                }
                verify(productService, times(1)).updateProduct(eq(productId), any(Product.class));
        }

        @Test
        void testUpdateSales_otherException() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                int soldCount = 10;

                doThrow(new RuntimeException("Database error")).when(productService).updateSales(eq(productId),
                                eq(soldCount));

                try {
                        mockMvc.perform(post("/products/update-sales/{id}", productId)
                                        .session(session)
                                        .param("soldCount", String.valueOf(soldCount)));
                } catch (Exception e) {
                        // Expected - exception should be re-thrown
                }
                verify(productService, times(1)).updateSales(eq(productId), eq(soldCount));
        }

        @Test
        void testUpdateProductImage_success() throws Exception {
                UUID userId = UUID.randomUUID();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("userId", userId);

                UUID productId = UUID.randomUUID();
                MockMultipartFile imageFile = new MockMultipartFile(
                                "imageFile",
                                "test.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "image content".getBytes());

                doNothing().when(productService).updateProductImage(eq(productId), any(MultipartFile.class));

                mockMvc.perform(multipart("/products/update-image/{id}", productId)
                                .file(imageFile)
                                .session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/products?success=true"));

                verify(productService, times(1)).updateProductImage(eq(productId), any(MultipartFile.class));
        }

}
