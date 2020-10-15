package com.ias.ecommerce.exception.customs;

public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message){
        super(message);
    }

    public AuthorizationException(){
        super("You cannot do this action");
    }

}
