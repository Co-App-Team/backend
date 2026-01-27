package com.backend.coapp.model.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RequestErrorCodeEnumTest {
    @Test
    public void expectContain(){
        RequestErrorCodeEnum requestHasNullOrEmptyField = RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD;
        assertNotNull(requestHasNullOrEmptyField);
    }

    @Test
    public void nameShouldMatch(){
        RequestErrorCodeEnum requestHasNullOrEmptyField = RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD;
        assertEquals("REQUEST_HAS_NULL_OR_EMPTY_FIELD",requestHasNullOrEmptyField.name());
    }
}
