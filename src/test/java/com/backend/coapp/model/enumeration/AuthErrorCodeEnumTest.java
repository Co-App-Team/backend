package com.backend.coapp.model.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AuthErrorCodeEnumTest {
    @Test
    public void expectContain(){
        AuthErrorCodeEnum invalidEmail = AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE;
        assertNotNull(invalidEmail);

        AuthErrorCodeEnum emailNotRegister = AuthErrorCodeEnum.EMAIL_NOT_REGISTERED;
        assertNotNull(emailNotRegister);

        AuthErrorCodeEnum existAccountWithEmail = AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL;
        assertNotNull(existAccountWithEmail);

        AuthErrorCodeEnum invalidConfirmationCode = AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE;
        assertNotNull(invalidConfirmationCode);
    }

    @Test
    public void nameShouldMatch(){
        AuthErrorCodeEnum invalidEmail = AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE;
        assertEquals("INVALID_CONFIRMATION_CODE",invalidEmail.name());

        AuthErrorCodeEnum emailNotRegister = AuthErrorCodeEnum.EMAIL_NOT_REGISTERED;
        assertEquals("EMAIL_NOT_REGISTERED",emailNotRegister.name());

        AuthErrorCodeEnum existAccountWithEmail = AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL;
        assertEquals("EXIST_ACCOUNT_WITH_EMAIL",existAccountWithEmail.name());

        AuthErrorCodeEnum invalidConfirmationCode = AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE;
        assertEquals("INVALID_CONFIRMATION_CODE",invalidConfirmationCode.name());

    }
}
