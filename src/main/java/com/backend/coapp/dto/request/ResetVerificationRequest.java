package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO Request for resetting verification code for each user.
 */

@Getter
@AllArgsConstructor
public class ResetVerificationRequest implements  IRequest{
    // JSON request keys
    private String email;

    @Override
    public void validateRequest() throws InvalidRequestException {
        if (this.email == null || this.email.isBlank()){
            throw  new InvalidRequestException("Email can NOT be null or empty.");
        }
    }
}
