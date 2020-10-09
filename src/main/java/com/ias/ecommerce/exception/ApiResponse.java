package com.ias.ecommerce.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ApiResponse {

    private String timestamp;
    private HttpStatus status;
    private String message;
    private Object data;
    private Integer codeStatus;
    private boolean error;

    public ApiResponse(){
        this.timestamp = LocalDateTime.now().toString();
    }

    public ApiResponse(HttpStatus status, String message, Object data, Integer codeStatus, boolean error){
        this.status = status;
        this.message = message;
        this.data = data;
        this.codeStatus = codeStatus;
        this.timestamp = LocalDateTime.now().toString();
        this.error = error;
    }

    public ApiResponse(HttpStatus status, String message, Integer codeStatus, boolean error){
        this.status = status;
        this.message = message;
        this.codeStatus = codeStatus;
        this.timestamp = LocalDateTime.now().toString();
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getCodeStatus() {
        return codeStatus;
    }

    public void setCodeStatus(Integer codeStatus) {
        this.codeStatus = codeStatus;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
