package com.ias.ecommerce.exception.customs;

public class OperationNotCompletedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OperationNotCompletedException(String message){
        super(message);
    }

}
