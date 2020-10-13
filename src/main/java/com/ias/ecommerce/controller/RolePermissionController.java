package com.ias.ecommerce.controller;

import com.ias.ecommerce.entity.Permission;
import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.PermissionRepository;
import com.ias.ecommerce.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RolePermissionController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionController(RoleRepository roleRepository, PermissionRepository permissionRepository){
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @PostMapping("/role_permission")
    public ResponseEntity<Object> associateRole(@RequestParam(name = "role_id") Integer roleId,
                                                @RequestParam(name = "permission_id") long permissionId){

        Role role = roleRepository.findById(roleId).orElseThrow( () -> new DataNotFoundException("The role with ID: "+roleId+" not exist."));
        Permission permission = permissionRepository.findById(permissionId).orElseThrow( () -> new DataNotFoundException("The permission with ID: "+permissionId+" not exist."));

        boolean alreadyExist = role.getPermissionList().stream().anyMatch(permissionFilter -> permissionFilter.getId() == permissionId);

        if(alreadyExist){
            throw new OperationNotCompletedException("Already exist the association. Role: "+role.getName()+", Permission: "+permission.getName());
        }

        permission.addRoleItem(role);
        role.addPermissionItem(permission);

        roleRepository.save(role);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("Role", role);
        objectMap.put("Permission", permission);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Association create successfully", objectMap, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
