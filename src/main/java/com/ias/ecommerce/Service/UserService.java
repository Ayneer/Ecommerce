package com.ias.ecommerce.Service;

import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.repository.UserRepository;

import java.util.Optional;

public class UserService {

    private UserRepository userRepository;

    public UserService(){}

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUserName(username);
    }

}
