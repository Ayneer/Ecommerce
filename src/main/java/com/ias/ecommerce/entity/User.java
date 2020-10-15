package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userName;
    private String password;
    private boolean enabled;
    private LocalDateTime lastFailedAccess;
    private Integer countFailedAccess;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private UserDetail userDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_ROLE", nullable = false)
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> productList;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orderList;

    public User(){}

    public User(String userName, String password, boolean enabled, LocalDateTime lastFailedAccess, Integer countFailedAccess){
        this.userName = validateEmptyValue(userName, 5, "user name");
        this.password = validatePassword(password);
        this.enabled = enabled;
        this.lastFailedAccess = lastFailedAccess;
        this.countFailedAccess = countFailedAccess;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = validateEmptyValue(userName, 5, "user name");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = validatePassword(password);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastFailedAccess() {
        return lastFailedAccess;
    }

    public void setLastFailedAccess(LocalDateTime lastFailedAccess) {
        this.lastFailedAccess = lastFailedAccess;
    }

    public Integer getCountFailedAccess() {
        return countFailedAccess;
    }

    public void setCountFailedAccess(Integer countFailedAccess) {
        this.countFailedAccess = countFailedAccess;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    private String validateEmptyValue(String value, int minLength, String nameValue){
        if(value.isEmpty() || value.trim().isEmpty() || value.length() < minLength){
            throw new OperationNotCompletedException("The "+nameValue+" can't be empty and must be minimum "+minLength+" character.");
        }

        return value.trim();
    }

    private String validatePassword(String password){

        if(password.isEmpty() || password.trim().isEmpty() || password.length() < 6){
            throw new OperationNotCompletedException("The password is wrong. Must be minimum 6 character");
        }

        return hasPassword(password);
    }

    private String hasPassword(String password){
        return BCrypt.hashpw(password.trim(), BCrypt.gensalt());
    }

    public boolean isValidPassword(String password){
        return BCrypt.checkpw(password, this.password);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", lastFailedAccess=" + lastFailedAccess +
                ", countFailedAccess=" + countFailedAccess +
                ", userDetail=" + userDetail +
                ", role=" + role +
                '}';
    }
}
