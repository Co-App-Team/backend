package com.backend.coapp.exception;

/**
 * This exception will be thrown if client tries to register a new account with an email that has been used.
 */

public class AuthEmailAlreadyUsedException extends RuntimeException{
    public AuthEmailAlreadyUsedException(){
        super("An account with that email already exists.");
    }

    public AuthEmailAlreadyUsedException(String message){
        super("An account with that email already exists. " + message);
    }
}
