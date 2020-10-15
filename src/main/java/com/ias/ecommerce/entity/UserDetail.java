package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;

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
        this.name = validateName(name);
        this.email = validateEmail(email);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
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

    private String validateName(String name){
        if(name.isEmpty() || name.trim().isEmpty() || name.length() < 3){
            throw new OperationNotCompletedException("The name can't be empty and must be minimum "+3+" character.");
        }

        return name.trim();
    }

    private String validateEmail(String email){
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);

        if(email.isEmpty() || !pattern.matcher(email).matches()){
            throw new IllegalArgumentException("The email address is invalid: "+email);
        }

        return email;
    }

    @Override
    public String toString() {
        return "UserDetail{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
