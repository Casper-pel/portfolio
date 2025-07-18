package ip.project.backend.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService();  // Create an instance of AuthService before each test
    }

    @Test
    public void testHashPassword() {
        // Arrange
        String rawPassword = "password123";

        // Act
        String hashedPassword = authService.hashPassword(rawPassword);

        // Assert
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(rawPassword, hashedPassword, "Hashed password should not be equal to the raw password");
    }

    @Test
    public void testVerifyPassword_CorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = authService.hashPassword(rawPassword);

        // Act
        boolean isVerified = authService.verifyPassword(hashedPassword, rawPassword);

        // Assert
        assertTrue(isVerified, "The password should be verified correctly");
    }

    @Test
    public void testVerifyPassword_IncorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = authService.hashPassword(rawPassword);
        String incorrectPassword = "wrongpassword";

        // Act
        boolean isVerified = authService.verifyPassword(hashedPassword, incorrectPassword);

        // Assert
        assertFalse(isVerified, "The password should not be verified with an incorrect password");
    }
}
