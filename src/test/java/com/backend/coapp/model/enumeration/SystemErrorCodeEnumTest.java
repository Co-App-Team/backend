package com.backend.coapp.model.enumeration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SystemErrorCodeEnumTest {
    @Test
    public void expectContain(){
        SystemErrorCodeEnum internalError = SystemErrorCodeEnum.INTERNAL_ERROR;
        assertNotNull(internalError);
    }

    @Test
    public void nameShouldMatch(){
        SystemErrorCodeEnum internalError = SystemErrorCodeEnum.INTERNAL_ERROR;
        assertEquals("INTERNAL_ERROR",internalError.name());
    }
}
