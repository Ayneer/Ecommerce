package com.ias.ecommerce.controller;

import com.ias.ecommerce.repository.UserDetailRepository;

public class UserDetailController {

    private final UserDetailRepository userDetailRepository;

    public UserDetailController(UserDetailRepository userDetailRepository){
        this.userDetailRepository = userDetailRepository;
    }

}
