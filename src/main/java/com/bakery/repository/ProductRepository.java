package com.bakery.repository;

import com.bakery.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByUserId(UUID userId);
    List<Product> findByCategory(String category);
    List<Product> findByIsAvailable(Boolean isAvailable);
    List<Product> findByStockLessThan(Integer stock);
}