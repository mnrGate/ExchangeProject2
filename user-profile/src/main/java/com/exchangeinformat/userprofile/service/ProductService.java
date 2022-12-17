package com.exchangeinformat.userprofile.service;

import com.exchangeinformat.userprofile.model.Product;

public interface ProductService {
    void createProduct(Product product);

    Product getProduct(Long id);

    void updateProduct(Product product);

    void deleteProduct(Long id);
}
