package com.backend.coapp.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthEmailAlreadyUsedExceptionTest {

    @Test
    public void constructor_whenInitNoArgs_expectDefaultMessage(){
        AuthEmailAlreadyUsedException exception = new AuthEmailAlreadyUsedException();

        assertNotNull(exception);
        assertEquals("An account with that email already exists.", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void constructor_whenInitMessage_expectWithMessage(){
        AuthEmailAlreadyUsedException exception = new AuthEmailAlreadyUsedException("foo");

        assertNotNull(exception);
        assertEquals("An account with that email already exists. foo", exception.getMessage());
        assertNull(exception.getCause());
    }
}
