package com.ias.ecommerce.service;

import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.entity.UserDetail;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserService {

    private static final Integer MAX_ATTEMPT_LOGIN = 3;
    private static final long TIME_BETWEEN_FAILED = 1; //In hours

    private UserRepository userRepository;

    public UserService(){}

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUserName(username);
    }

    public User createUser(String userName, String password, String name, String email, Role role){

        userRepository.findByUserName(userName).ifPresent(user -> {
            throw new OperationNotCompletedException("Already exist a user with the username: "+userName);
        });

        User newUser = new User(userName, password, true, null, 0);
        UserDetail newUserDetail = new UserDetail(name, email);

        newUserDetail.setUser(newUser);
        newUser.setUserDetail(newUserDetail);
        newUser.setRole(role);

        return userRepository.save(newUser);
    }

    public User checkUserCredentials(String userName, String password){

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

    public Map<String, Object> mapUserInfo(User user, Role role, UserDetail userDetail){
        Map<String, Object> userMapInfo = new HashMap<>();
        userMapInfo.put("User", user);
        userMapInfo.put("Role", role);
        userMapInfo.put("UserDetails", userDetail);
        return userMapInfo;
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
