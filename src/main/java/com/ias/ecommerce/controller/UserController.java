package com.ias.ecommerce.controller;

import com.ias.ecommerce.security.Auth;
import com.ias.ecommerce.service.RoleService;
import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.repository.RoleRepository;
import com.ias.ecommerce.repository.UserRepository;
import com.ias.ecommerce.security.JwtUtil;
import com.ias.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserController {
    private static final String MESSAGE_USER_NOT_FOUND = "Does not exist a user with ID: ";

    private static final String CREATE_USER_EMPLOYEE = "CREATE_USER_EMPLOYEE";
    private static final String READ_ALL_USERS = "READ_ALL_USERS";
    private static final String UPDATE_ALL_USERS = "UPDATE_ALL_USERS";
    private static final String DELETE_ALL_USERS = "DELETE_ALL_USERS";

    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final Auth auth;

    UserController(UserRepository userRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.roleService = new RoleService(roleRepository);
        this.jwtUtil = new JwtUtil();
        this.userService = new UserService(userRepository);
        this.auth = new Auth(new UserService(userRepository));
    }

    @PostMapping("/user/employee")
    public ResponseEntity<Object> createEmployee(@RequestParam(value = "userName") String userName,
                                 @RequestParam(value = "password") String password,
                                 @RequestParam(value = "name") String name,
                                 @RequestParam(value = "email") String email) {

        if(!auth.isAdmin() || !auth.hasPermission(CREATE_USER_EMPLOYEE)){
            throw new AuthorizationException();
        }

        Role employeeRole = roleService.findByName(Auth.getEmployeeRoleName());
        User newUser = userService.createUser(userName, password, name, email, employeeRole);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Employee user created successfully", newUser, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/user/costumer")
    public ResponseEntity<Object> createCostumer(@RequestParam(value = "userName") String userName,
                                         @RequestParam(value = "password") String password,
                                         @RequestParam(value = "name") String name,
                                         @RequestParam(value = "email") String email) {

        Role costumerRole = roleService.findByName(Auth.getCostumerRoleName());
        User newUser = userService.createUser(userName, password, name, email, costumerRole);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Costumer user created successfully", newUser, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<Object> readAll(){

        if(!auth.isAdmin() || !auth.hasPermission(READ_ALL_USERS)){
            throw new AuthorizationException();
        }

        List<User> userList = new ArrayList<>();
        userRepository.findAll().forEach(userList::add);
        userList.removeIf(user -> user.getId().equals(auth.getUserAuth().getId()));

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Get all users successfully", userList, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Object> readOne(@PathVariable(value = "id") long id){

        if(!auth.isAdmin() || !auth.hasPermission(READ_ALL_USERS)){
            throw new AuthorizationException();
        }

        User user = userRepository.findById(id).orElseThrow( () -> new DataNotFoundException(MESSAGE_USER_NOT_FOUND+id) );
        Role role = new Role(user.getRole().getName(), user.getRole().getDescription(), user.getRole().getId());

        Map<String, Object> stringObjectMap = userService.mapUserInfo(user, role, user.getUserDetail());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Get user successfully", stringObjectMap, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/user/current")
    public ResponseEntity<Object> readUserCurrent(){

        User user = auth.getUserAuth();
        Role role = new Role(user.getRole().getName(), user.getRole().getDescription(), user.getRole().getId());

        Map<String, Object> stringObjectMap = userService.mapUserInfo(user, role, user.getUserDetail());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User info current get successfully", stringObjectMap, HttpStatus.OK.value(), false);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/user/details")
    public ResponseEntity<Object> update(@RequestParam(value = "name") String name,
                                         @RequestParam(value = "email") String email){

        User user = auth.getUserAuth();

        user.getUserDetail().setName(name);
        user.getUserDetail().setEmail(email);
        userRepository.save(user);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User updated successfully", user, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/user/lock/{id}")
    public ResponseEntity<Object> changeUserLock(@RequestParam(value = "blockUser") boolean blockUser,
                                                 @PathVariable(value = "id") long id){

        if(!auth.isAdmin() || !auth.hasPermission(UPDATE_ALL_USERS)){
            throw new AuthorizationException();
        }

        User user = userRepository.findById(id).orElseThrow( () -> new DataNotFoundException(MESSAGE_USER_NOT_FOUND+id) );

        user.setEnabled(!blockUser);
        user.setLastFailedAccess(null);
        user.setCountFailedAccess(0);
        userRepository.save(user);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User "+ (blockUser ? "locked" : "unlocked") +" successfully", user, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @DeleteMapping("/user/{id}")
    public ResponseEntity<Object> delete(@PathVariable(value = "id") long id){

        if(!auth.isAdmin() || !auth.hasPermission(DELETE_ALL_USERS)){
            throw new AuthorizationException();
        }

        User user = userRepository.findById(id).orElseThrow( () -> new DataNotFoundException(MESSAGE_USER_NOT_FOUND+id) );

        userRepository.delete(user);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User deleted successfully", user, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Object> logIn(@RequestParam(value = "userName") String userName,
                                        @RequestParam(value = "password") String password){

        User user = userService.checkUserCredentials(userName, password);
        Role role = new Role(user.getRole().getName(), user.getRole().getDescription(), user.getRole().getId());

        String token = jwtUtil.getJwtToken(userName, role.getName());

        Map<String, Object> stringObjectMap = userService.mapUserInfo(user, role, user.getUserDetail());
        stringObjectMap.put("Token", token);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Login successfully", stringObjectMap, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}

