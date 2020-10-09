package com.ias.ecommerce.exception.customs;

public class DataNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1l;

    public DataNotFoundException(String message){
        super(message);
    }

}
