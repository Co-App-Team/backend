package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ResetVerificationRequestTest {
    @Test
    public void getterMethod_expectInitValues(){
        ResetVerificationRequest request = new ResetVerificationRequest("foo@mail.com");

        assertEquals("foo@mail.com",request.getEmail());
    }

    @Test
    public void validateRequest_whenInvalidEmail_expectException(){
        ResetVerificationRequest requestBlank = new ResetVerificationRequest("");
        assertThrows(InvalidRequestException.class, requestBlank::validateRequest);

        ResetVerificationRequest requestNull = new ResetVerificationRequest(null);
        assertThrows(InvalidRequestException.class, requestNull::validateRequest);
    }

    @Test
    public void validateRequest_whenValidEmail_expectNoException(){
        ResetVerificationRequest request = new ResetVerificationRequest("foo@mail.com");
        assertDoesNotThrow(request::validateRequest);
    }
}
