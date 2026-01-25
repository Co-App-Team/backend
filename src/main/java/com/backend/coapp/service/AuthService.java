package com.backend.coapp.service;

import com.backend.coapp.exception.AuthEmailAlreadyUsed;
import com.backend.coapp.exception.AuthEmailNotRegistered;
import com.backend.coapp.exception.EmailInvalidAddress;
import com.backend.coapp.exception.EmailServiceException;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Authentication Service
 *
 * <p>This handles all business logic related to authentication.
 */

@Service
@Slf4j
public class AuthService {

    public static final int NUMS_VERIFICATION_CODE = 6;

    /** Singleton service and repository **/
    private final UserRepository userRepository;
    private final EmailService emailService;


    @Autowired
    public AuthService(UserRepository userRepository, EmailService emailService){
        this.userRepository = userRepository;
        this.emailService = emailService;

    }

    /**
     * Create a new user and send verification code to user email.
     *
     * @param email user email
     * @param password password
     * @param firstName User first name
     * @param lastName User last name
     * @throws AuthEmailAlreadyUsed if provided email has been used with an existing account
     * @throws EmailServiceException if there is a failure in EmailService
     * @throws EmailInvalidAddress if provided email has invalid format
     */
    public void createNewUser(String email, String password, String firstName, String lastName) throws AuthEmailAlreadyUsed, EmailServiceException, EmailInvalidAddress {
        UserModel userIfExist = this.userRepository.findUserModelByEmail(email);

        if (userIfExist != null){
            log.warn("WARNING LOG: User tried to create a new account with an email that has been used.");
            throw new AuthEmailAlreadyUsed();
        }else{
            int verificationCode = this.generateVerificationCode();
            String emailSubject = "Verification code for your new account";
            String emailBody = """
                Dear user,
    
                Your confirmation code is: %d
    
                Please do NOT share this code.
        
                Thank you,
                CoApp Team.
                """.formatted(verificationCode);
            this.emailService.sendEmail(email,emailSubject,emailBody);

            UserModel newUser = new UserModel(email,password,firstName,lastName, verificationCode);
            this.userRepository.save(newUser);
        }

    }

    /**
     * Verify confirmation code to activate new account
     *
     * @param email user email
     * @param verifyCode verification code
     * @return true if user is successfully verified; false otherwise
     * @throws AuthEmailNotRegistered if there aren't any accounts associated with provided email.
     */
    public boolean verifyUser(String email, int verifyCode) throws AuthEmailNotRegistered {
        UserModel user = this.userRepository.findUserModelByEmail(email);

        if (user == null){
            throw new AuthEmailNotRegistered();
        }else{
            boolean verified = user.getVerified();
            if (verified){
                return true;
            }

            user.setVerified(user.getVerificationCode() == verifyCode);
            verified = user.getVerified();
            if (verified){
                user.setVerificationCode(-1);
                //@TODO This operation may fail if there is something wrong with database
                this.userRepository.save(user);
            }

            return verified;
        }
    }

    /**
     * Reset verification code and resend the new code to user email.
     * @param email user email
     * @throws EmailServiceException if there is failure with EmailService
     * @throws AuthEmailNotRegistered if there aren't any accounts associated with provided email.
     */
    public void resetVerifyCode(String email) throws  EmailServiceException, AuthEmailNotRegistered{
        UserModel user = this.userRepository.findUserModelByEmail(email);

        if (user == null){
            throw new AuthEmailNotRegistered();
        }else{
            int newVerifyCode = this.generateVerificationCode();

            String emailSubject = "Verification code for your new account";
            String emailBody = """
                Dear user,
    
                Your confirmation code is: %d
    
                Please do NOT share this code.
        
                Thank you,
                CoApp Team.
                """.formatted(newVerifyCode);
            this.emailService.sendEmail(email,emailSubject,emailBody);

            user.setVerificationCode(newVerifyCode);
            this.userRepository.save(user);
        }
    }

    /**
     * Generate verification code of NUMS_VERIFICATION_CODE digits.
     *
     * @return int
     */
    private int generateVerificationCode(){
        Random random = new Random();
        int lowerBound = (int) Math.pow(10,NUMS_VERIFICATION_CODE - 1);
        int upperRange = lowerBound * 9;
        return lowerBound + random.nextInt(upperRange);
    }

}
