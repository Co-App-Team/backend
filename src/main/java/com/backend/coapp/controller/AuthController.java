package com.backend.coapp.controller;

import com.backend.coapp.dto.request.ResetVerificationRequest;
import com.backend.coapp.dto.request.UserRegisterRequest;
import com.backend.coapp.dto.request.VerifyEmailRequest;
import com.backend.coapp.exception.*;
import com.backend.coapp.model.enumeration.AuthErrorCodeEnum;
import com.backend.coapp.model.enumeration.RequestErrorCodeEnum;
import com.backend.coapp.model.enumeration.SystemErrorCodeEnum;
import com.backend.coapp.service.AuthService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@Getter // For testing
@RequestMapping("/api/auth")
public class AuthController {

    /** Singleton service and repository **/
    private final AuthService authService;


    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;

    }

    /**
     * Register a new account with a valid email.
     * This method will send verification code to user email, which will be used to activate the account.
     *
     * @param registerRequest UserRegisterRequest
     * @return ResponseEntity
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody UserRegisterRequest registerRequest) {
        try{
            registerRequest.validateRequest();
            this.authService.createNewUser(registerRequest.getEmail(),registerRequest.getPassword(), registerRequest.getFirstName(), registerRequest.getLastName());

        }catch (InvalidRequestException e){
            return ResponseEntity.status(400).body(Map.of("error", RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD, "message",e.getMessage()));
        } catch (EmailInvalidAddressException e){
            return ResponseEntity.status(400).body(Map.of("error",AuthErrorCodeEnum.INVALID_EMAIL,"message",e.getMessage()));
        } catch (EmailServiceException e){
            String errorMessage = "ERROR: Email service failed: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(500).body(Map.of("error", SystemErrorCodeEnum.INTERNAL_ERROR,"message","Unable to send verification email. Please try again later."));
        } catch (AuthEmailAlreadyUsedException e){
            return ResponseEntity.status(409).body(Map.of("error", AuthErrorCodeEnum.EXIST_ACCOUNT_WITH_EMAIL, "message", e.getMessage()));
        } catch (RuntimeException e){
            String errorMessage = "ERROR: Create account service failed: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(500).body(Map.of("error", SystemErrorCodeEnum.INTERNAL_ERROR,"message","Unable to create a new account. Please try again later."));
        }

        return ResponseEntity.ok().body(Map.of("message","An confirmation code will be sent to your email. Please provide the confirmation to activate your account."));
    }

    /**
     * Activate the account if the user provides correct verification code.
     *
     * @param verifyEmailRequest VerifyEmailRequest
     * @return ResponseEntity
     */
    @PatchMapping("/verify-email")
    public ResponseEntity<Map<String,Object>> verifyEmail (@RequestBody VerifyEmailRequest verifyEmailRequest){
         boolean successVerify = false;

        try{
            verifyEmailRequest.validateRequest();
            successVerify = this.authService.verifyUser(verifyEmailRequest.getEmail(),verifyEmailRequest.getVerifyCode());

        }catch (InvalidRequestException e){
            return ResponseEntity.status(400).body(Map.of("error", RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD, "message", e.getMessage()));
        }catch (AuthEmailNotRegisteredException e){
            return ResponseEntity.status(400).body(Map.of("error", AuthErrorCodeEnum.EMAIL_NOT_REGISTERED, "message", e.getMessage()));
        }catch (Exception e){
            String errorMessage = "ERROR: Verify email service failed: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(500).body(Map.of("error", SystemErrorCodeEnum.INTERNAL_ERROR,"message","Unable to verify email. Please try again later."));
        }

        if (successVerify){
            return ResponseEntity.ok().body(Map.of("message","Your account is verified."));

        }else{
            return ResponseEntity.status(400).body(Map.of("error", AuthErrorCodeEnum.INVALID_CONFIRMATION_CODE,"message", "Invalid confirmation code."));
        }
    }

    /**
     * This API will reset verification code and send the new code to user email.
     *
     * @param resetVerificationRequest ResetVerificationRequest
     * @return ResponseEntity
     */
    @PatchMapping("/reset-confirmation-code")
    public ResponseEntity<Map<String,Object>> resetVerificationCode(@RequestBody ResetVerificationRequest resetVerificationRequest){
        try{
            resetVerificationRequest.validateRequest();
            this.authService.resetVerifyCode(resetVerificationRequest.getEmail());
        }catch (InvalidRequestException e){
            return ResponseEntity.status(400).body(Map.of("error", RequestErrorCodeEnum.REQUEST_HAS_NULL_OR_EMPTY_FIELD, "message", e.getMessage()));
        }catch (AuthEmailNotRegisteredException e){
            return ResponseEntity.status(400).body(Map.of("error", AuthErrorCodeEnum.EMAIL_NOT_REGISTERED, "message", e.getMessage()));
        }catch (EmailServiceException e){
            String errorMessage = "ERROR: Email service failed: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(500).body(Map.of("error", SystemErrorCodeEnum.INTERNAL_ERROR,"message","Unable to send verification email. Please try again later."));
        }catch (Exception e){
            String errorMessage = "ERROR: Reset verification code service failed: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(500).body(Map.of("error", SystemErrorCodeEnum.INTERNAL_ERROR,"message","Unable to reset verification code. Please try again later."));
        }

        return ResponseEntity.ok().body(Map.of("message","An confirmation code will be sent to your email."));

    }
}
