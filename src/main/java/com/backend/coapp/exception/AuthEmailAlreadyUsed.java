package com.backend.coapp.exception;

/**
 * This exception will be thrown if client tries to register a new account with an email that has been used.
 */

public class AuthEmailAlreadyUsed extends RuntimeException{
    public AuthEmailAlreadyUsed(){
        super("An account with that email already exists.");
    }

    public AuthEmailAlreadyUsed(String message){
        super("An account with that email already exists. " + message);
    }

}
