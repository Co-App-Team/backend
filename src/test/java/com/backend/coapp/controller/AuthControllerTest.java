package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.*;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import com.backend.coapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthService authService;

  @Autowired private AuthController authController;

  private UserRegisterRequest dummyUserRegisterRequest;
  private VerifyEmailRequest dummyVerifyEmailRequest;
  private ResetVerificationRequest dummyResetVerificationRequest;
  private ForgotPasswordRequest dummyForgotPasswordRequest;
  private UpdatePasswordRequest dummyUpdatePasswordRequest;

  @BeforeEach
  void setUp() {
    dummyUserRegisterRequest = new UserRegisterRequest("foo@mail.com", "123", "foo", "woof");

    dummyVerifyEmailRequest = new VerifyEmailRequest("foo@mail.com", 123);

    dummyResetVerificationRequest = new ResetVerificationRequest("foo@mail.com");
    dummyForgotPasswordRequest = new ForgotPasswordRequest("foo@mail.com");
    dummyUpdatePasswordRequest = new UpdatePasswordRequest("foo@mail.com", 123, "newPassword");
  }

  @Test
  public void constructor_expectSameInitInstances() {
    assertEquals(this.authService, this.authController.getAuthService());
  }

  @Test
  public void createAccount_whenInvalidRequest_expect400Response() throws Exception {
    UserRegisterRequest request = mock(UserRegisterRequest.class);
    doThrow(new InvalidRequestException()).when(request).validateRequest();
    UserRegisterRequest invalidRequest = new UserRegisterRequest(null, null, null, null);

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()));

    verifyNoInteractions(authService);
  }

  @Test
  public void createAccount_whenEmailInvalidAddress_expect400Response() throws Exception {
    doThrow(new EmailInvalidAddressException())
        .when(this.authService)
        .createNewUser(anyString(), anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUserRegisterRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.INVALID_EMAIL.name()));

    verify(authService, times(1))
        .createNewUser(
            this.dummyUserRegisterRequest.getEmail(),
            this.dummyUserRegisterRequest.getPassword(),
            this.dummyUserRegisterRequest.getFirstName(),
            this.dummyUserRegisterRequest.getLastName());
  }

  @Test
  public void createAccount_whenEmailServiceFail_expect500Response() throws Exception {
    doThrow(new EmailServiceException())
        .when(this.authService)
        .createNewUser(anyString(), anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUserRegisterRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()));

    verify(authService, times(1))
        .createNewUser(
            this.dummyUserRegisterRequest.getEmail(),
            this.dummyUserRegisterRequest.getPassword(),
            this.dummyUserRegisterRequest.getFirstName(),
            this.dummyUserRegisterRequest.getLastName());
  }

  @Test
  public void createAccount_whenEmailAlreadyUsed_expect409Response() throws Exception {
    String errorMessage = "foo message.";
    doThrow(new AuthEmailAlreadyUsedException(errorMessage))
        .when(this.authService)
        .createNewUser(anyString(), anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUserRegisterRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL.name()));

    verify(authService, times(1))
        .createNewUser(
            this.dummyUserRegisterRequest.getEmail(),
            this.dummyUserRegisterRequest.getPassword(),
            this.dummyUserRegisterRequest.getFirstName(),
            this.dummyUserRegisterRequest.getLastName());
  }

  @Test
  public void createAccount_whenUnknowFailure_expect500Response() throws Exception {
    String errorMessage = "foo message.";
    doThrow(new RuntimeException(errorMessage))
        .when(this.authService)
        .createNewUser(anyString(), anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUserRegisterRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()));

    verify(authService, times(1))
        .createNewUser(
            this.dummyUserRegisterRequest.getEmail(),
            this.dummyUserRegisterRequest.getPassword(),
            this.dummyUserRegisterRequest.getFirstName(),
            this.dummyUserRegisterRequest.getLastName());
  }

  @Test
  public void createAccount_whenEverythingSuccess_expect200Response() throws Exception {

    doNothing().when(authService).createNewUser(anyString(), anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUserRegisterRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .createNewUser(
            this.dummyUserRegisterRequest.getEmail(),
            this.dummyUserRegisterRequest.getPassword(),
            this.dummyUserRegisterRequest.getFirstName(),
            this.dummyUserRegisterRequest.getLastName());
  }

  @Test
  public void verifyEmail_whenInvalidRequest_expect400Response() throws Exception {
    VerifyEmailRequest invalidRequest = new VerifyEmailRequest(null, null);
    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verifyNoInteractions(authService);
  }

  @Test
  public void verifyEmail_whenEmailNotRegistered_expect400Response() throws Exception {
    String errorMessage = "foo message.";
    doThrow(new AuthEmailNotRegisteredException(errorMessage))
        .when(this.authService)
        .verifyUser(anyString(), anyInt());

    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyVerifyEmailRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.EMAIL_NOT_REGISTERED.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .verifyUser(
            this.dummyVerifyEmailRequest.getEmail(), this.dummyVerifyEmailRequest.getVerifyCode());
  }

  @Test
  public void verifyEmail_whenUnknowFailure_expect500Response() throws Exception {
    doThrow(new RuntimeException()).when(this.authService).verifyUser(anyString(), anyInt());

    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyVerifyEmailRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .verifyUser(
            this.dummyVerifyEmailRequest.getEmail(), this.dummyVerifyEmailRequest.getVerifyCode());
  }

  @Test
  public void verifyEmail_whenCorrectCode_expect200Response() throws Exception {
    doReturn(true).when(this.authService).verifyUser(anyString(), anyInt());
    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyVerifyEmailRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .verifyUser(
            this.dummyVerifyEmailRequest.getEmail(), this.dummyVerifyEmailRequest.getVerifyCode());
  }

  @Test
  public void verifyEmail_whenIncorrectCode_expect400Response() throws Exception {
    doReturn(false).when(this.authService).verifyUser(anyString(), anyInt());
    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyVerifyEmailRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .verifyUser(
            this.dummyVerifyEmailRequest.getEmail(), this.dummyVerifyEmailRequest.getVerifyCode());
  }

  @Test
  public void resetVerificationCode_whenInvalidRequest_expect400Response() throws Exception {
    ResetVerificationRequest invalidRequest = new ResetVerificationRequest(null);
    mockMvc
        .perform(
            patch("/api/auth/reset-confirmation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verifyNoInteractions(authService);
  }

  @Test
  public void resetVerificationCode_whenEmailNotRegistered_expect400Response() throws Exception {
    doThrow(new AuthEmailNotRegisteredException())
        .when(this.authService)
        .resetVerifyCode(anyString());

    mockMvc
        .perform(
            patch("/api/auth/reset-confirmation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyResetVerificationRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.EMAIL_NOT_REGISTERED.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(authService, times(1)).resetVerifyCode(this.dummyResetVerificationRequest.getEmail());
  }

  @Test
  public void resetVerificationCode_whenEmailServiceFail_expect500Response() throws Exception {
    doThrow(new EmailServiceException()).when(this.authService).resetVerifyCode(anyString());

    mockMvc
        .perform(
            patch("/api/auth/reset-confirmation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyResetVerificationRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(authService, times(1)).resetVerifyCode(this.dummyResetVerificationRequest.getEmail());
  }

  @Test
  public void resetVerificationCode_whenUnknowFailure_expect500Response() throws Exception {
    doThrow(new RuntimeException()).when(this.authService).resetVerifyCode(anyString());

    mockMvc
        .perform(
            patch("/api/auth/reset-confirmation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyResetVerificationRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1)).resetVerifyCode(this.dummyResetVerificationRequest.getEmail());
  }

  @Test
  void resetVerificationCode_whenEverythingSuccess_expect200Response() throws Exception {
    doNothing().when(authService).resetVerifyCode(anyString());

    mockMvc
        .perform(
            patch("/api/auth/reset-confirmation-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyResetVerificationRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(authService, times(1)).resetVerifyCode(this.dummyResetVerificationRequest.getEmail());
  }

  @Test
  public void forgotPassword_whenAccountNotActivatedYet_expect401Response() throws Exception {
    doThrow(new AuthAccountNotYetActivatedException())
        .when(this.authService)
        .forgotPassword(anyString());

    mockMvc
        .perform(
            patch("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyForgotPasswordRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.ACCOUNT_NOT_ACTIVATED.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(authService, times(1)).forgotPassword(this.dummyForgotPasswordRequest.getEmail());
  }

  @Test
  void forgotPassword_whenEverythingSuccess_expect200Response() throws Exception {
    doNothing().when(authService).forgotPassword(anyString());

    mockMvc
        .perform(
            patch("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyForgotPasswordRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(authService, times(1)).forgotPassword(this.dummyForgotPasswordRequest.getEmail());
  }

  @Test
  public void updatePassword_whenCorrectCode_expect200Response() throws Exception {
    doReturn(true).when(this.authService).updatePassword(anyString(), anyInt(), anyString());
    mockMvc
        .perform(
            patch("/api/auth/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUpdatePasswordRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .updatePassword(
            this.dummyUpdatePasswordRequest.getEmail(),
            this.dummyUpdatePasswordRequest.getVerifyCode(),
            this.dummyUpdatePasswordRequest.getNewPassword());
  }

  @Test
  public void updatePassword_whenIncorrectCode_expect400Response() throws Exception {
    doReturn(false).when(this.authService).updatePassword(anyString(), anyInt(), anyString());
    mockMvc
        .perform(
            patch("/api/auth/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyUpdatePasswordRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .updatePassword(
            this.dummyUpdatePasswordRequest.getEmail(),
            this.dummyUpdatePasswordRequest.getVerifyCode(),
            this.dummyUpdatePasswordRequest.getNewPassword());
  }
}
