package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissions")
public class Permission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "permissionList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Role> roleList = null;

    public Permission(){
        this.roleList = new ArrayList<>();
    }

    public Permission(String name, String description){
        this.name = validateName(name);
        this.description = validateDescription(description);
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public void addRoleItem(Role role){
        this.roleList.add(role);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
