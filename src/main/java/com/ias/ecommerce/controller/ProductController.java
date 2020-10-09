package com.ias.ecommerce.controller;

import com.ias.ecommerce.Service.UserService;
import com.ias.ecommerce.entity.Product;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.repository.ProductRepository;
import com.ias.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    private final ProductRepository productRepository;
    private final UserService userService;
    private static final String DO_NOT_EXIST = " do not exist.";

    public ProductController(ProductRepository productRepository, UserRepository userRepository){
        this.productRepository = productRepository;
        this.userService = new UserService(userRepository);
    }

    @PostMapping("/product")
    public ResponseEntity<Object> create(@RequestParam(value = "name") String name,
                                         @RequestParam(value = "description") String description,
                                         @RequestParam(value = "basePrice") float basePrice,
                                         @RequestParam(value = "taxRate") float taxRate,
                                         @RequestParam(value = "productStatus") String productStatus,
                                         @RequestParam(value = "inventoryQuantity") Integer inventoryQuantity){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userService.findByUsername(username).orElseThrow(() -> new DataNotFoundException("The User with ID: "+username+DO_NOT_EXIST));

        Product product = new Product(name, description, basePrice, taxRate, inventoryQuantity);
        product.setProductStatus(productStatus);

        List<Product> productList = new ArrayList<>();
        productList.add(product);
        user.setProductList(productList);

        product.setUser(user);
        productRepository.save(product);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Product created successfully", product, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<Object> findByUsername(){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        //String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().toString();

        User user = userService.findByUsername(username).orElseThrow(() -> new DataNotFoundException("The User with ID: "+username+DO_NOT_EXIST));

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Products", user.getProductList(), HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Object> update(@RequestParam(value = "name") String name,
                                         @RequestParam(value = "description") String description,
                                         @RequestParam(value = "basePrice") float basePrice,
                                         @RequestParam(value = "taxRate") float taxRate,
                                         @RequestParam(value = "productStatus") String productStatus,
                                         @RequestParam(value = "inventoryQuantity") Integer inventoryQuantity,
                                         @PathVariable(value = "id") long id){

         String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
         Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("The product with ID: "+id+DO_NOT_EXIST));

         if(!product.getUser().getUserName().equals(username)){
             throw new AuthorizationException("You can not update this resource.");
         }

         product.setName(name);
         product.setDescription(description);
         product.setBasePrice(basePrice);
         product.setTaxRate(taxRate);
         product.setProductStatus(productStatus);
         product.setInventoryQuantity(inventoryQuantity);

         productRepository.save(product);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Product updated successfully", product, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Object> delete(@PathVariable(value = "id") long id){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("The product with ID: "+id+DO_NOT_EXIST));

        if(!product.getUser().getUserName().equals(username)){
            throw new AuthorizationException("You can not delete this resource.");
        }

        productRepository.delete(product);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Product deleted successfully", product, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
