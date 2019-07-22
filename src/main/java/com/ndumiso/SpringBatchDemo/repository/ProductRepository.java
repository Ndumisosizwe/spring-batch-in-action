package com.ndumiso.SpringBatchDemo.repository;

import com.ndumiso.SpringBatchDemo.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
