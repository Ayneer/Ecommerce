package com.ias.ecommerce.controller;

import com.ias.ecommerce.Service.RoleService;
import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.entity.UserDetail;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.RoleRepository;
import com.ias.ecommerce.repository.UserRepository;
import com.ias.ecommerce.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private static final Integer MAX_ATTEMPT_LOGIN = 3;
    private static final long TIME_BETWEEN_FAILED = 1; //In hours
    private final JwtUtil jwtUtil;

    UserController(UserRepository userRepository, RoleRepository roleRepository){
        this.userRepository = userRepository;
        this.roleService = new RoleService(roleRepository);
        this.jwtUtil = new JwtUtil();
    }

    @PostMapping("/user")
    public ResponseEntity<Object> create(@RequestParam(value = "userName") String userName,
                                 @RequestParam(value = "password") String password,
                                 @RequestParam(value = "name") String name,
                                 @RequestParam(value = "email") String email,
                                 @RequestParam(value = "roleId") int roleId) {

        Optional<User> userOptional = userRepository.findByUserName(userName);
        Role roleFound = roleService.getById(roleId);

        if(userOptional.isPresent()){
            throw new OperationNotCompletedException("Already exist a user with the username: "+userName);
        }

        User newUser = new User(userName, password, true, null, 0);
        UserDetail newUserDetail = new UserDetail(name, email);

        newUserDetail.setUser(newUser);
        newUser.setUserDetail(newUserDetail);
        newUser.setRole(roleFound);

        userRepository.save(newUser);
        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User created successfully", newUser, HttpStatus.OK.value(), false);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/user")
    public ResponseEntity<Object> update(@RequestParam(value = "name") String name,
                                         @RequestParam(value = "email") String email){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Optional<User> userOptional = userRepository.findByUserName(username);

        if(userOptional.isEmpty()){
            throw new OperationNotCompletedException("The User with username: "+username+" do not exist.");
        }

        userOptional.get().getUserDetail().setName(name);
        userOptional.get().getUserDetail().setEmail(email);
        userRepository.save(userOptional.get());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User updated successfully", userOptional.get(), HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Object> delete(@PathVariable(value = "id") long id){

        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isEmpty()){
            throw new OperationNotCompletedException("The User with id: "+id+" do not exist.");
        }

        userRepository.delete(userOptional.get());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User deleted successfully", userOptional.get(), HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers(){

        List<User> userList = new ArrayList<>();
        userRepository.findAll().forEach(userList::add);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Get all users successfully", userList, HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/user/current")
    public ResponseEntity<Object> getUserCurrent(){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Optional<User> userOptional = userRepository.findByUserName(username);

        if(userOptional.isEmpty()){
            throw new OperationNotCompletedException("The User with username: "+username+" do not exist.");
        }

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("User", userOptional.get());
        stringObjectMap.put("Role", userOptional.get().getRole());
        stringObjectMap.put("UserDetails", userOptional.get().getUserDetail());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User info current got successfully", stringObjectMap, HttpStatus.OK.value(), false);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Object> logIn(@RequestParam(value = "userName") String userName,
                        @RequestParam(value = "password") String password){

        User user = checkUserCredentials(userName, password);

        String token = jwtUtil.getJwtToken(userName, user.getRole().getName());

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("User", user);
        stringObjectMap.put("Role", user.getRole());
        stringObjectMap.put("UserDetails", user.getUserDetail());
        stringObjectMap.put("Token", token);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Login successfully", stringObjectMap, HttpStatus.OK.value(), false);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/user/lock/{username}")
    public ResponseEntity<Object> changeUserLock(@RequestParam(value = "blockUser") boolean blockUser,
                                            @PathVariable(value = "username") String username){

        Optional<User> optionalUser = userRepository.findByUserName(username);

        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("The user do not exist.");
        }

        optionalUser.get().setEnabled(blockUser);
        optionalUser.get().setLastFailedAccess(null);
        optionalUser.get().setCountFailedAccess(0);
        userRepository.save(optionalUser.get());

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "User update successfully", optionalUser.get(), HttpStatus.OK.value(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private User checkUserCredentials(String userName, String password){

        Optional<User> userOptional = userRepository.findByUserName(userName);

        if(userOptional.isPresent() && !userOptional.get().isEnabled()){
            throw new AuthorizationException("The account is blocked, get in touch with your admin to enable it.");
        }

        if(userOptional.isEmpty() || !userOptional.get().isValidPassword(password)){
            String errorMessage = "The username or password is invalid, please check it and try again.";
            if(userOptional.isPresent()){
                errorMessage = verifyFailedAccess(userOptional.get());
            }
            throw new AuthorizationException(errorMessage);
        }

        return userOptional.get();
    }

    private String verifyFailedAccess(User user){

        Integer countFailedAccess = user.getCountFailedAccess();
        LocalDateTime nowDate = LocalDateTime.now();
        String message = "The username or password is invalid, please check it and try again.";

        if(countFailedAccess == 0){
            user.setLastFailedAccess(LocalDateTime.now());
            user.setCountFailedAccess(1);
            userRepository.save(user);
            return message + " Have 2 attempts left.";
        }

        Integer newCountFailedAccess = getCountFailedAccessByTime(user, nowDate);
        user.setLastFailedAccess(nowDate);
        user.setCountFailedAccess(newCountFailedAccess);

        if(newCountFailedAccess >= MAX_ATTEMPT_LOGIN){
            user.setEnabled(false);
            message = "The account has been blocked, get in touch with your admin to enable it.";
        }else{
            message = message + " Have "+(MAX_ATTEMPT_LOGIN - newCountFailedAccess)+" attempts left.";
        }

        userRepository.save(user);
        return message;
    }

    private Integer getCountFailedAccessByTime(User user, LocalDateTime nowDate){

        long hours = ChronoUnit.HOURS.between(user.getLastFailedAccess(), nowDate);
        Integer countFailedAccess = user.getCountFailedAccess();

        if(hours <= TIME_BETWEEN_FAILED){
            countFailedAccess++;
        }else{
            countFailedAccess = 1;
        }

        return countFailedAccess;
    }

}

