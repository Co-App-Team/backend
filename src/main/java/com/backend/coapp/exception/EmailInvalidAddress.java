package com.backend.coapp.exception;

/**
 * This exception will be thrown if the provided email is invalid.
 */

public class EmailInvalidAddress extends RuntimeException{
    public EmailInvalidAddress(){
        super("Invalid email or email not exit");
    }

    public EmailInvalidAddress(String message){
        super("Invalid email or email not exit. " + message);
    }
}
