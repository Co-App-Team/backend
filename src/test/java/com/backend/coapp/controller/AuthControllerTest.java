package com.backend.coapp.controller;

import com.backend.coapp.dto.request.ResetVerificationRequest;
import com.backend.coapp.dto.request.UserRegisterRequest;
import com.backend.coapp.dto.request.VerifyEmailRequest;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import com.backend.coapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {
    private AuthService authService;
    private  AuthController authController;
    private UserRegisterRequest dummyUserRegisterRequest;
    private VerifyEmailRequest dummyVerifyEmailRequest;
    private ResetVerificationRequest dummyResetVerificationRequest;

    @BeforeEach
    public void setUp(){
        this.authService = mock(AuthService.class);
        this.authController = new AuthController(this.authService);
        this.dummyUserRegisterRequest = new UserRegisterRequest("foo@mail.com","123","foo","woof");
        this.dummyVerifyEmailRequest = new VerifyEmailRequest("foo@mail.com",123);
        this.dummyResetVerificationRequest = new ResetVerificationRequest("foo@mail.com");
    }

    @Test
    public void constructor_expectSameInitInstances(){
        assertEquals(this.authService, this.authController.getAuthService());
    }

    @Test
    public void createAccount_whenInvalidRequest_expect400Response(){
        UserRegisterRequest request = mock(UserRegisterRequest.class);
        String errorMessage = "foo message.";
        doThrow(new InvalidRequestException(errorMessage)).when(request).validateRequest();

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertTrue(responseMessage.contains(errorMessage));
        verifyNoInteractions(authService);
    }

    @Test
    public void createAccount_whenEmailInvalidAddress_expect400Response(){
        String errorMessage = "foo message.";
        doThrow(new EmailInvalidAddressException(errorMessage)).when(this.authService).createNewUser(anyString(), anyString(), anyString(), anyString());

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(this.dummyUserRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(AuthErrorCodeEnum.INVALID_EMAIL,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertTrue(responseMessage.contains(errorMessage));
        verify(authService,times(1));
    }

    @Test
    public void createAccount_whenEmailServiceFail_expect500Response(){
        String errorMessage = "foo message.";
        doThrow(new EmailServiceException(errorMessage)).when(this.authService).createNewUser(anyString(), anyString(), anyString(), anyString());

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(this.dummyUserRegisterRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(SystemErrorCodeEnum.INTERNAL_ERROR,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    @Test
    public void createAccount_whenEmailAlreadyUsed_expect409Response(){
        String errorMessage = "foo message.";
        doThrow(new AuthEmailAlreadyUsedException(errorMessage)).when(this.authService).createNewUser(anyString(), anyString(), anyString(), anyString());

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(this.dummyUserRegisterRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertTrue(responseMessage.contains(errorMessage));
        verify(authService,times(1));
    }

    @Test
    public void createAccount_whenUnknowFailure_expect500Response(){
        String errorMessage = "foo message.";
        doThrow(new RuntimeException(errorMessage)).when(this.authService).createNewUser(anyString(), anyString(), anyString(), anyString());

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(this.dummyUserRegisterRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(SystemErrorCodeEnum.INTERNAL_ERROR,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    @Test
    public void createAccount_whenEverythingSuccess_expect200Response(){

        ResponseEntity<Map<String, Object>> response = this.authController.createAccount(this.dummyUserRegisterRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    @Test
    public void verifyEmail_whenInvalidRequest_expect400Response(){
        VerifyEmailRequest request = mock(VerifyEmailRequest.class);
        String errorMessage = "foo message.";
        doThrow(new InvalidRequestException(errorMessage)).when(request).validateRequest();

        ResponseEntity<Map<String, Object>> response = this.authController.verifyEmail(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertTrue(responseMessage.contains(errorMessage));
        verifyNoInteractions(authService);
    }

    @Test
    public void verifyEmail_whenEmailNotRegistered_expect400Response(){
        String errorMessage = "foo message.";
        doThrow(new AuthEmailNotRegisteredException(errorMessage)).when(this.authService).verifyUser(anyString(), anyInt());

        ResponseEntity<Map<String, Object>> response = this.authController.verifyEmail(this.dummyVerifyEmailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(AuthErrorCodeEnum.EMAIL_NOT_REGISTERED,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertTrue(responseMessage.contains(errorMessage));
        verify(authService,times(1));
    }

    @Test
    public void verifyEmail_whenUnknowFailure_expect500Response(){
        String errorMessage = "foo message.";
        doThrow(new RuntimeException(errorMessage)).when(this.authService).verifyUser(anyString(),anyInt());

        ResponseEntity<Map<String, Object>> response = this.authController.verifyEmail(this.dummyVerifyEmailRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(SystemErrorCodeEnum.INTERNAL_ERROR,
                response.getBody().get("error"));
        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    @Test
    public void verifyEmail_whenCorrectCode_expect200Response(){
        doReturn(true).when(this.authService).verifyUser(anyString(),anyInt());
        ResponseEntity<Map<String, Object>> response = this.authController.verifyEmail(this.dummyVerifyEmailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    @Test
    public void verifyEmail_whenIncorrectCode_expect400Response(){
        doReturn(false).when(this.authService).verifyUser(anyString(),anyInt());
        ResponseEntity<Map<String, Object>> response = this.authController.verifyEmail(this.dummyVerifyEmailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        String responseMessage = (String) response.getBody().get("message");
        assertNotNull(responseMessage);
        assertFalse(responseMessage.isBlank());
        verify(authService,times(1));
    }

    //TODO: Add unit test for resetVerificationCode
}
