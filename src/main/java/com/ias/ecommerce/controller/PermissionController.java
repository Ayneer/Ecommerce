package com.ias.ecommerce.controller;

import com.ias.ecommerce.entity.Permission;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.PermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository){
        this.permissionRepository = permissionRepository;
    }

    @PostMapping("/permission")
    public ResponseEntity<Object> create(@RequestParam(name = "name") String name,
                                         @RequestParam(name = "description") String description){

        permissionRepository.findByName(name).ifPresent(permission -> {
            throw new OperationNotCompletedException("Already exist a permission with this name: "+name);
        });

        Permission permission = new Permission(name, description);
        permissionRepository.save(permission);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "permission create successfully", permission, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/permission")
    public ResponseEntity<Object> readAll(){
        List<Permission> permissionList = new ArrayList<>();
        permissionRepository.findAll().forEach(permissionList::add);
        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "permissions read successfully", permissionList, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/permission/{id}")
    public ResponseEntity<Object> readOne(@PathVariable(name = "id") long id){
        Permission permission = permissionRepository.findById(id).orElseThrow( () -> permissionNotFound(id));
        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "permission read successfully", permission, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/permission/{id}")
    public ResponseEntity<Object> update(@RequestParam(name = "name") String name,
                                         @RequestParam(name = "description") String description,
                                         @PathVariable(name = "id") long id){

        Permission permission = permissionRepository.findById(id).orElseThrow(() -> permissionNotFound(id));

        permissionRepository.findByName(name).ifPresentOrElse(permission1 -> {
            if(permission1.getId() != id){
                throw new OperationNotCompletedException("Already exist a permission with this name: "+name);
            }
        }, null);

        permission.setName(name);
        permission.setDescription(description);

        permissionRepository.save(permission);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "permission update successfully", permission, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/permission/{id}")
    public ResponseEntity<Object> delete(@PathVariable(name = "id") long id){

        Permission permission = permissionRepository.findById(id).orElseThrow(() -> permissionNotFound(id));
        permissionRepository.delete(permission);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "permission delete successfully", permission, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private DataNotFoundException permissionNotFound(long id){
        return new DataNotFoundException("The permission with ID: "+id+" do not exist.");
    }
}
