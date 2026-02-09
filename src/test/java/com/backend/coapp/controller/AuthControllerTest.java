package com.backend.coapp.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.coapp.dto.request.*;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import com.backend.coapp.service.AuthService;
import com.backend.coapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtService jwtService;

  @Autowired private AuthController authController;

  private UserRegisterRequest dummyUserRegisterRequest;
  private VerifyEmailRequest dummyVerifyEmailRequest;
  private ResetVerificationRequest dummyResetVerificationRequest;
  private ForgotPasswordRequest dummyForgotPasswordRequest;
  private UpdatePasswordRequest dummyUpdatePasswordRequest;
  private LoginRequest dummyLoginRequest;

  @BeforeEach
  void setUp() {
    dummyUserRegisterRequest = new UserRegisterRequest("foo@mail.com", "123", "foo", "woof");

    dummyVerifyEmailRequest = new VerifyEmailRequest("foo@mail.com", 123);

    dummyResetVerificationRequest = new ResetVerificationRequest("foo@mail.com");
    dummyForgotPasswordRequest = new ForgotPasswordRequest("foo@mail.com");
    dummyUpdatePasswordRequest = new UpdatePasswordRequest("foo@mail.com", 123, "newPassword");
    dummyLoginRequest = new LoginRequest("foo@mail.com", "password");
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
  public void verifyEmail_whenAccountAlreadyVerified_expect405Response() throws Exception {
    doThrow(new AuthAccountAlreadyVerifyException())
        .when(this.authService)
        .verifyUser(anyString(), anyInt());
    mockMvc
        .perform(
            patch("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyVerifyEmailRequest)))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.ACCOUNT_ALREADY_VERIFIED.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .verifyUser(
            this.dummyVerifyEmailRequest.getEmail(), this.dummyVerifyEmailRequest.getVerifyCode());
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
    doNothing().when(this.authService).verifyUser(anyString(), anyInt());
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
    doThrow(new IncorrectCodeException()).when(this.authService).verifyUser(anyString(), anyInt());
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
    doNothing().when(this.authService).updatePassword(anyString(), anyInt(), anyString());
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
    doThrow(new IncorrectCodeException())
        .when(this.authService)
        .updatePassword(anyString(), anyInt(), anyString());
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

  @Test
  public void login_whenBadCredential_expect401Response() throws Exception {
    doThrow(new AuthBadCredentialException())
        .when(this.authService)
        .login(anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyLoginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.INVALID_EMAIL_OR_PASSWORD.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .login(this.dummyLoginRequest.getEmail(), this.dummyLoginRequest.getPassword());
  }

  @Test
  public void login_whenAccountNotYetActivated_expect401Response() throws Exception {
    doThrow(new AuthAccountNotYetActivatedException())
        .when(this.authService)
        .login(anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyLoginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value(AuthErrorCodeEnum.ACCOUNT_NOT_ACTIVATED.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .login(this.dummyLoginRequest.getEmail(), this.dummyLoginRequest.getPassword());
  }

  @Test
  public void login_whenJwtServiceFail_expect500Response() throws Exception {
    doThrow(new JwtServiceFailException("foo message"))
        .when(this.authService)
        .login(anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyLoginRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .login(this.dummyLoginRequest.getEmail(), this.dummyLoginRequest.getPassword());
  }

  @Test
  public void login_whenAuthFail_expect500Response() throws Exception {
    doThrow(
            new LockedException(
                "foo")) // Since we don't support lock user account yet. This should be internal
        // error if isLocked() return True
        .when(this.authService)
        .login(anyString(), anyString());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyLoginRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCodeEnum.INTERNAL_ERROR.name()))
        .andExpect(jsonPath("$.message").isNotEmpty());
    verify(authService, times(1))
        .login(this.dummyLoginRequest.getEmail(), this.dummyLoginRequest.getPassword());
  }

  @Test
  void login_whenSuccess_expectCookieInHeader() throws Exception {
    String expectedToken = "jwt-token-12345";
    long expirationSeconds = 3600L;

    when(authService.login(anyString(), anyString())).thenReturn(expectedToken);
    when(authService.getTokenExpireDurationInSeconds()).thenReturn(expirationSeconds);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dummyLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(header().exists("Set-Cookie"))
        .andExpect(header().string("Set-Cookie", containsString("Authorization=" + expectedToken)))
        .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", containsString("Secure")))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
        .andExpect(header().string("Set-Cookie", containsString("Max-Age=" + expirationSeconds)))
        .andExpect(header().string("Set-Cookie", containsString("Path=/")))
        .andExpect(jsonPath("$.message").value("Logged in successfully."));
    verify(authService, times(1))
        .login(this.dummyLoginRequest.getEmail(), this.dummyLoginRequest.getPassword());
    verify(authService, times(1)).getTokenExpireDurationInSeconds();
  }

  @Test
  void logout_whenSuccess_expectUnsetCookie() throws Exception {
    mockMvc
        .perform(get("/api/auth/logout").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().exists("Set-Cookie"))
        .andExpect(header().string("Set-Cookie", containsString("Authorization=")))
        .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", containsString("Secure")))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
        .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
        .andExpect(header().string("Set-Cookie", containsString("Path=/")));
  }
}
