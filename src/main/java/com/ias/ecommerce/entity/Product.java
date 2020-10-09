package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    public enum ProductStatus {
        BORRADOR,
        PUBLICADO;

        public static boolean isMember(String productStatusName) {
            for (ProductStatus productStatus : ProductStatus.values()) {
                if (productStatus.name().equals(productStatusName)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 280)
    private String description;

    @Column(nullable = false)
    private float basePrice;

    @Column(nullable = false)
    private float taxRate;

    private ProductStatus productStatus;
    private Integer inventoryQuantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_USER", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItemList;

    public Product() {
    }

    public Product(String name, String description, float basePrice, float taxRate, Integer inventoryQuantity) {
        this.name = validateString(name, 100, "name");
        this.description = validateString(description, 280, "description");
        this.basePrice = validateBasePrice(basePrice);
        this.taxRate = validateTaxRate(taxRate);
        this.inventoryQuantity = validateInventoryQuantity(inventoryQuantity);
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateString(name, 100, "name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = validateString(description, 280, "description");
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = validateBasePrice(basePrice);
    }

    public float getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(float taxRate) {
        this.taxRate = validateTaxRate(taxRate);
    }

    public Integer getInventoryQuantity() {
        return inventoryQuantity;
    }

    public void setInventoryQuantity(Integer inventoryQuantity) {
        this.inventoryQuantity = validateInventoryQuantity(inventoryQuantity);
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
    }

    public void setProductStatus(String productStatus) {
        if(!ProductStatus.isMember(productStatus)){
            throw new OperationNotCompletedException("The product status is not recognized");
        }
        this.productStatus = ProductStatus.valueOf(productStatus);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderItem> getOrderItem() {
        return orderItemList;
    }

    public void setOrderItem(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    private String validateString(String name, int maxLength, String key){
        if(name == null || name.isEmpty() || name.length() > maxLength){
            throw new OperationNotCompletedException("The product "+key+" must not be null or empty and must be maximum "+maxLength+" character.");
        }

        return name.trim();
    }

    private float validateBasePrice(float basePrice){
        if(basePrice < 0){
            throw new OperationNotCompletedException("The product base price must be greater than 0");
        }
        return basePrice;
    }

    private float validateTaxRate(float taxRate){
        if(taxRate < 0 || taxRate > 1){
            throw new OperationNotCompletedException("The product tax rate must be greater than 0 and less than 1");
        }
        return taxRate;
    }

    private Integer validateInventoryQuantity(Integer inventoryQuantity){
        if(inventoryQuantity < 0){
            throw new OperationNotCompletedException("The product inventory quantity must not be less than 0");
        }
        return inventoryQuantity;
    }
}
