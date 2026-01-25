package com.backend.coapp.exception;

/**
 * This exception will be thrown when client tries to access information with unregistered email.
 */

public class AuthEmailNotRegistered extends RuntimeException {
    public AuthEmailNotRegistered(){
        super("Email is not yet registered.");
    }

    public AuthEmailNotRegistered(String message){
        super("Email is not yet registered." + message);
    }
}
