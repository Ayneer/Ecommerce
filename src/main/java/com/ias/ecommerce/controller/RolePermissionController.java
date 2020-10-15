package com.ias.ecommerce.controller;

import com.ias.ecommerce.entity.Permission;
import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.PermissionRepository;
import com.ias.ecommerce.repository.RoleRepository;
import com.ias.ecommerce.repository.UserRepository;
import com.ias.ecommerce.security.Auth;
import com.ias.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RolePermissionController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final Auth auth;

    public RolePermissionController(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository){
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auth = new Auth( new UserService(userRepository));
    }

    @PostMapping("/role_permission/associate")
    public ResponseEntity<Object> associateRole(@RequestParam(name = "role_id") Integer roleId,
                                                @RequestParam(name = "permission_id") long permissionId){

        if(!auth.isAdmin()){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(roleId).orElseThrow( () -> roleNotFound(roleId));
        Permission permission = permissionRepository.findById(permissionId).orElseThrow( () -> permissionNotFound(permissionId));

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

    @PostMapping("/role_permission/disassociate")
    public ResponseEntity<Object> disassociateRole(@RequestParam(name = "role_id") Integer roleId,
                                                @RequestParam(name = "permission_id") long permissionId){

        if(!auth.isAdmin()){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(roleId).orElseThrow( () -> roleNotFound(roleId));
        Permission permission = permissionRepository.findById(permissionId).orElseThrow( () -> permissionNotFound(permissionId));

        boolean alreadyExist = role.getPermissionList().stream().anyMatch(permissionFilter -> permissionFilter.getId() == permissionId);

        if(!alreadyExist){
            throw new OperationNotCompletedException("Do not exist this association. Role: "+role.getName()+", Permission: "+permission.getName());
        }

        permission.getRoleList().removeIf(role1 -> role1.getId().equals(roleId));
        role.getPermissionList().removeIf(permission1 -> permission1.getId() == permissionId);

        roleRepository.save(role);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("Role", role);
        objectMap.put("Permission", permission);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Disassociation create successfully", objectMap, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/role_permission/role/{id}")
    public ResponseEntity<Object> getPermissionByRole(@PathVariable(name = "id") Integer id){

        if(!auth.isAdmin()){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(id).orElseThrow( () -> roleNotFound(id) );

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Role permissions get successfully", role.getPermissionList(), HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    public DataNotFoundException roleNotFound(Integer id){
        return new DataNotFoundException("The role with ID: "+id+" does not exist.");
    }

    public DataNotFoundException permissionNotFound(long id){
        return new DataNotFoundException("The permission with ID: "+id+" does not exist.");
    }

}
