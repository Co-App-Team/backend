package com.backend.coapp.controller;

import com.backend.coapp.dto.request.*;
import com.backend.coapp.service.AuthService;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter // For testing
@RequestMapping("/api/auth")
public class AuthController {

  /** Singleton service and repository * */
  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Register a new account with a valid email. This method will send verification code to user
   * email, which will be used to activate the account.
   *
   * @param registerRequest UserRegisterRequest
   * @return ResponseEntity
   */
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> createAccount(
      @RequestBody UserRegisterRequest registerRequest) {
    registerRequest.validateRequest();
    this.authService.createNewUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName());

    return ResponseEntity.ok()
        .body(
            Map.of(
                "message",
                "A confirmation code will be sent to your email. Please provide the confirmation to activate your account."));
  }

  /**
   * Activate the account if the user provides correct verification code.
   *
   * @param verifyEmailRequest VerifyEmailRequest
   * @return ResponseEntity
   */
  @PatchMapping("/verify-email")
  public ResponseEntity<Map<String, Object>> verifyEmail(
      @RequestBody VerifyEmailRequest verifyEmailRequest) {
    boolean successVerify = false;

    verifyEmailRequest.validateRequest();
    this.authService.verifyUser(verifyEmailRequest.getEmail(), verifyEmailRequest.getVerifyCode());
    return ResponseEntity.ok().body(Map.of("message", "Your account is verified."));
  }

  /**
   * This API will reset verification code and send the new code to user email.
   *
   * @param resetVerificationRequest ResetVerificationRequest
   * @return ResponseEntity
   */
  @PatchMapping("/reset-confirmation-code")
  public ResponseEntity<Map<String, Object>> resetVerificationCode(
      @RequestBody ResetVerificationRequest resetVerificationRequest) {
    resetVerificationRequest.validateRequest();
    this.authService.resetVerifyCode(resetVerificationRequest.getEmail());

    return ResponseEntity.ok()
        .body(Map.of("message", "A confirmation code will be sent to your email."));
  }

  /**
   * This API will set forgot password code and send the code to user email.
   *
   * @param forgotPasswordRequest ForgotPasswordRequest
   * @return ResponseEntity
   */
  @PatchMapping("/forgot-password")
  public ResponseEntity<Map<String, Object>> forgotPassword(
      @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    forgotPasswordRequest.validateRequest();
    this.authService.forgotPassword(forgotPasswordRequest.getEmail());

    return ResponseEntity.ok()
        .body(
            Map.of(
                "message",
                "A confirmation code will be sent to your email. Please provide the confirmation code to reset your password."));
  }

  /**
   * This API will set forgot password code and send the code to user email.
   *
   * @param updatePasswordRequest UpdatePasswordRequest
   * @return ResponseEntity
   */
  @PatchMapping("/update-password")
  public ResponseEntity<Map<String, Object>> updatePassword(
      @RequestBody UpdatePasswordRequest updatePasswordRequest) {
    updatePasswordRequest.validateRequest();
    this.authService.updatePassword(
        updatePasswordRequest.getEmail(),
        updatePasswordRequest.getVerifyCode(),
        updatePasswordRequest.getNewPassword());
    return ResponseEntity.ok().body(Map.of("message", "Password was updated successfully."));
  }
}
