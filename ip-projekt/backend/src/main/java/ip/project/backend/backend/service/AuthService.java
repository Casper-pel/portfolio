package ip.project.backend.backend.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication-related operations such as password hashing and verification.
 * This service uses the Argon2 algorithm for secure password hashing, which is considered
 * one of the most secure hashing algorithms available.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Hashes a raw password using the Argon2 algorithm.
     * 
     * @param rawPassword The plain text password to be hashed
     * @return A string containing the hashed password
     */
    public String hashPassword(String rawPassword){
        logger.debug("Hashing password with Argon2 algorithm");
        Argon2 argon2 = Argon2Factory.create();
        try{
            String hashedPassword = argon2.hash(2, 65536, 1, rawPassword);
            logger.info("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error hashing password: {}", e.getMessage(), e);
            throw e;
        } finally {
            argon2.wipeArray(rawPassword.toCharArray());
            logger.debug("Password array wiped from memory");
        }
    }

    /**
     * Verifies if an entered password matches a stored hashed password.
     * 
     * @param hashedPassword The hashed password stored in the database
     * @param enteredPassword The plain text password entered by the user
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String hashedPassword, String enteredPassword){
        logger.debug("Verifying password with Argon2 algorithm");
        Argon2 argon2 = Argon2Factory.create();
        try {
            boolean isValid = argon2.verify(hashedPassword, enteredPassword);
            if (isValid) {
                logger.debug("Password verification successful");
            } else {
                logger.debug("Password verification failed - password does not match");
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying password: {}", e.getMessage(), e);
            throw e;
        } finally {
            argon2.wipeArray(enteredPassword.toCharArray());
            logger.debug("Password array wiped from memory");
        }
    }
}
