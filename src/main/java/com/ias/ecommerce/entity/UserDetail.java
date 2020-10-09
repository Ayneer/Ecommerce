package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.regex.Pattern;

@Entity
@Table(name = "user_details")
public class UserDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String email;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_USER", nullable = false)
    @JsonIgnore
    private User user;

    public UserDetail(){}

    public UserDetail(String name, String email){
        this.name = validateEmptyValue(name, 5,"name");
        this.email = validateEmail(email);
    }

    private String validateEmptyValue(String value, int minLength, String nameValue){
        if(value.isEmpty() || value.trim().isEmpty() || value.length() < minLength){
            throw new IllegalArgumentException("The "+nameValue+" can't be empty and must be minimum "+minLength+" character.");
        }

        return value.trim();
    }

    private Integer validateRoleId(Integer roleId){
        if(roleId <= 0){
            throw new IllegalArgumentException("Invalid roleId, please fix it. "+roleId);
        }
        return roleId;
    }

    private String validateEmail(String email){
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);

        if(email.isEmpty() || !pattern.matcher(email).matches()){
            throw new IllegalArgumentException("The email address is invalid: "+email);
        }

        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", auth=" + user +
                '}';
    }
}
