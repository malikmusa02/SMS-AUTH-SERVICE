package com.java.sms.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message){
        super(message);
    }
}
