package com.ias.ecommerce.exception;

import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @NotNull ResponseEntity<Object> handleHttpMessageNotReadable(@NotNull HttpMessageNotReadableException exception, @NotNull HttpHeaders headers, @NotNull HttpStatus status, @NotNull WebRequest request){
        String errorMessage = "Malformed JSON request";
        ApiResponse apiResponse = new ApiResponse(HttpStatus.BAD_REQUEST, errorMessage, HttpStatus.BAD_REQUEST.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({OperationNotCompletedException.class})
    protected ResponseEntity<Object> handleOperationNotCompleted(OperationNotCompletedException exception){
        ApiResponse apiResponse = new ApiResponse(HttpStatus.CONFLICT, exception.getMessage(), HttpStatus.CONFLICT.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({DataNotFoundException.class})
    protected ResponseEntity<Object> handlerDataNotFound(DataNotFoundException exception){
        ApiResponse apiResponse = new ApiResponse(HttpStatus.NOT_FOUND, exception.getMessage(), HttpStatus.NOT_FOUND.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AuthorizationException.class})
    protected ResponseEntity<Object> handlerAuthorizationError(AuthorizationException exception){
        ApiResponse apiResponse = new ApiResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), HttpStatus.UNAUTHORIZED.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

}
