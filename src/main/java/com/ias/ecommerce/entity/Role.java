package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "FK_ROLE"),
            inverseJoinColumns = @JoinColumn(name = "FK_PERMISSION")
    )
    private List<Permission> permissionList;

    public Role(){
        this.users = new ArrayList<>();
        this.permissionList = new ArrayList<>();
    }

    public Role(String name, String description){
        this.name = validateName(name);
        this.description = validateDescription(description);
    }

    public Role(String name, String description, Integer id){
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.id = id;
    }

    private String validateName(String name){
        if(name.isEmpty() || name.trim().isEmpty()){
            throw new OperationNotCompletedException("The name can't be empty");
        }

        return name.trim();
    }

    private String validateDescription(String description){
        if(description.isEmpty() || description.trim().isEmpty() || description.length() < 5){
            throw new OperationNotCompletedException("The description can't be empty and must be minimum "+5+" character.");
        }

        return description.trim();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = validateDescription(description);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }

    public void addPermissionItem(Permission permission){
        this.permissionList.add(permission);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
