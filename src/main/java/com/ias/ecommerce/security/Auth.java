package com.ias.ecommerce.security;

import com.ias.ecommerce.service.UserService;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

public class Auth {

    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String COSTUMER_ROLE_NAME = "COSTUMER";
    private static final String EMPLOYEE_ROLE_NAME = "EMPLOYEE";

    private final UserService userService;

    public Auth(UserService userService){
        this.userService = userService;
    }

    public User getUserAuth(){
        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userService.findByUsername(username).orElseThrow(() -> new DataNotFoundException("The User : "+username+" do not exist."));
    }

    public String getRoleName(){
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().toString();
    }

    public boolean isAdmin(){
        return ADMIN_ROLE_NAME.equals(getRoleName());
    }

    public boolean isCostumer(){
        return COSTUMER_ROLE_NAME.equals(getRoleName());
    }

    public boolean isEmployee(){
        return EMPLOYEE_ROLE_NAME.equals(getRoleName());
    }

}
