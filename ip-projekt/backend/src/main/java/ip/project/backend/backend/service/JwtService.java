package ip.project.backend.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for handling JWT (JSON Web Token) operations.
 * This service provides functionality for token generation, validation, and data extraction.
 * It uses HMAC-SHA256 for signing tokens and includes employee ID as the subject claim.
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

//    private static final String SECRET = "my-very-secret-key-that-should-be-very-long"; // > 256 bit
    private final String SECRET = System.getenv("SIGNING_KEY");
    private static final long EXPIRATION_TIME = 86400000; // 24h


    /**
     * Creates a signing key from the secret for JWT token operations.
     * 
     * @return A Key object used for signing and verifying JWT tokens
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Validates a JWT token by checking its signature and expiration.
     * 
     * @param token The JWT token string to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(final String token) {
        logger.debug("Validating JWT token");
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            logger.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username from a JWT token.
     * The username is stored as the subject claim in the token.
     * 
     * @param token The JWT token string to extract the username from
     * @return The username extracted from the token
     * @throws Exception If the token is invalid or the username cannot be extracted
     */
    public String extractUsername(final String token) {
        logger.debug("Extracting username from JWT token");
        try {
            String username = extractAllClaims(token).getSubject();
            logger.debug("Successfully extracted username: {}", username);
            return username;
        } catch (Exception e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the employee ID from a JWT token.
     * The employee ID is stored as the subject claim in the token and is parsed as an Integer.
     * 
     * @param token The JWT token string to extract the employee ID from
     * @return The employee ID extracted from the token, or null if extraction fails
     */
    public Integer extractEmployeeId(final String token) {
        logger.debug("Extracting employee ID from JWT token");
        try {
            String sub = extractAllClaims(token).getSubject();
            Integer employeeId = Integer.valueOf(sub);
            logger.debug("Successfully extracted employee ID: {} from token", employeeId);
            return employeeId;
        } catch (Exception e) {
            logger.warn("Failed to extract employee ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts all claims from a JWT token.
     * This is a helper method used by other extraction methods.
     * 
     * @param token The JWT token string to extract claims from
     * @return The Claims object containing all claims from the token
     * @throws Exception If the token is invalid or claims cannot be extracted
     */
    private Claims extractAllClaims(final String token) {
        logger.debug("Extracting all claims from JWT token");
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.debug("Successfully extracted claims from token");
            return claims;
        } catch (Exception e) {
            logger.warn("Failed to extract claims from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Generates a new JWT token for the given employee ID.
     * The token includes the employee ID as both the subject and a custom claim,
     * an issued-at timestamp, and an expiration time.
     * 
     * @param employeeId The employee ID to include in the token
     * @return A JWT token string
     * @throws Exception If token generation fails
     */
    public String generateToken(final Integer employeeId){
        logger.debug("Generating JWT token for employee ID: {}", employeeId);
        try {
            String token = Jwts.builder()
                    .setSubject(String.valueOf(employeeId))
                    .claim("employeeId", employeeId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
            logger.debug("Successfully generated JWT token for employee ID: {}", employeeId);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate JWT token for employee ID {}: {}", employeeId, e.getMessage(), e);
            throw e;
        }
    }

}
