package com.backend.coapp.exception;

/**
 * This exception will be thrown if the provided email is invalid.
 */

public class EmailInvalidAddressException extends RuntimeException{
    public EmailInvalidAddressException(){
        super("Invalid email or email not exit.");
    }

    public EmailInvalidAddressException(String message){
        super("Invalid email or email not exit. " + message);
    }
}
