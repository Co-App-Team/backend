package com.backend.coapp.dto.request;

import com.backend.coapp.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO request for new user account registration.
 */

@Getter
@AllArgsConstructor
public class UserRegisterRequest implements IRequest{
    // JSON request keys
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    @Override
    public void validateRequest() throws InvalidRequestException {
        if (
            this.email == null || this.email.isBlank()
            || this.password == null || this.password.isBlank()
            || this.firstName == null || this.firstName.isBlank()
            || this.lastName == null || this.lastName.isBlank()
        ){
            throw new InvalidRequestException("Email, password, firstname and lastname can NOT be null or empty");
        }
    }
}
