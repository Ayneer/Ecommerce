package com.ias.ecommerce.service;

import com.ias.ecommerce.entity.Product;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.repository.ProductRepository;

public class ProductService {
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Product findById(long id){
        return productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("The Product with ID: "+id+" does not exist."));
    }

    public Product update(Product product){
        return productRepository.save(product);
    }
}
