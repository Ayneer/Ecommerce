package com.ias.ecommerce.controller;

import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.RoleRepository;
import com.ias.ecommerce.repository.UserRepository;
import com.ias.ecommerce.security.Auth;
import com.ias.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RoleController {

    private static final String CREATE_ROLE = "CREATE_ROLE";
    private static final String READ_ROLE = "READ_ROLE";
    private static final String UPDATE_ROLE = "UPDATE_ROLE";
    private static final String DELETE_ROLE = "DELETE_ROLE";

    private final RoleRepository roleRepository;
    private final Auth auth;

    public RoleController(RoleRepository roleRepository, UserRepository userRepository){
        this.roleRepository = roleRepository;
        this.auth = new Auth( new UserService(userRepository));
    }

    @PostMapping("/role")
    public ResponseEntity<Object> create(@RequestParam(value = "name") String name,
                        @RequestParam(value = "description") String description){

        if(!auth.isAdmin() || !auth.hasPermission(CREATE_ROLE)){
            throw new AuthorizationException();
        }

        roleRepository.findByName(name).ifPresent(permission -> {
            throw new OperationNotCompletedException("Already exist a role with this name: "+name);
        });

        Role roleToSave = new Role(name, description);

        roleRepository.save(roleToSave);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "role create successfully", roleToSave, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/role")
    public ResponseEntity<Object> readAll(){

        if(!auth.isAdmin() || !auth.hasPermission(READ_ROLE)){
            throw new AuthorizationException();
        }

        List<Role> roleList = new ArrayList<>();
        roleRepository.findAll().forEach(roleList::add);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "roles read successfully", roleList, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/role/{id}")
    public ResponseEntity<Object> readOne(@PathVariable(value = "id") Integer id){

        if(!auth.isAdmin() || !auth.hasPermission(READ_ROLE)){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(id).orElseThrow( () -> roleNotFound(id) );

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "role read successfully", role, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<Object> update(@RequestParam(name = "name") String name,
                                         @RequestParam(name = "description") String description,
                                         @PathVariable(name = "id") Integer id){

        if(!auth.isAdmin() || !auth.hasPermission(UPDATE_ROLE)){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(id).orElseThrow(() -> roleNotFound(id));

        roleRepository.findByName(name).ifPresentOrElse(role1 -> {
            if(!role1.getId().equals(id)){
                throw new OperationNotCompletedException("Already exist a role with this name: "+name);
            }
        }, null);

        role.setName(name);
        role.setDescription(description);

        roleRepository.save(role);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "role update successfully", role, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/role/{id}")
    public ResponseEntity<Object> delete(@PathVariable(name = "id") Integer id){

        if(!auth.isAdmin() || !auth.hasPermission(DELETE_ROLE)){
            throw new AuthorizationException();
        }

        Role role = roleRepository.findById(id).orElseThrow(() -> roleNotFound(id));
        roleRepository.delete(role);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "role delete successfully", role, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private DataNotFoundException roleNotFound(Integer id){
        return new DataNotFoundException("The role with ID: "+id+" do not exist.");
    }

}
