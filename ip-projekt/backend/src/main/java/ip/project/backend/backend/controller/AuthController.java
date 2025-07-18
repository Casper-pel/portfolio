package ip.project.backend.backend.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.AuthService;
import ip.project.backend.backend.service.EmployeeService;
import ip.project.backend.backend.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentifizierung", description = "API zur Benutzeranmeldung, -registrierung und -abmeldung")
public class AuthController {
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String TOKEN = "token";
    private final EmployeeRepository employeeRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final EmployeeService employeeService;

    private final AuthService authService;

    private final JwtService jwtService;

    @Autowired
    public AuthController(EmployeeRepository employeeRepository, EmployeeService employeeService, AuthService authService, JwtService jwtService) {
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Benutzer-Login", description = "Authentifiziert einen Benutzer anhand der Mitarbeiter-ID und des Passworts. Gibt bei Erfolg ein JWT-Cookie zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login erfolgreich"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "401", description = "Falsche Anmeldedaten"),
            @ApiResponse(responseCode = "404", description = "Mitarbeiter-ID nicht gefunden"),
            @ApiResponse(responseCode = "500", description = "Serverfehler")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Mitarbeiterdaten mit ID und Passwort", required = true)
            @RequestBody EmployeeDto loginRequest,
            HttpServletResponse response
    ) {
        try {
            Integer employeeId = loginRequest.getEmployeeId();
            String password = loginRequest.getPassword();

            logger.info("Login attempt for employee ID: {}", employeeId);

            String jwtToken = jwtService.generateToken(employeeId);
            if (jwtToken == null) {
                logger.error("Failed to generate JWT token for employee ID: {}", employeeId);
                return ResponseEntity.badRequest().build();
            }

            Cookie cookie = new Cookie(TOKEN, jwtToken);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            logger.debug("JWT cookie created for employee ID: {}", employeeId);

            Optional<Employee> employeeOptional = employeeRepository.findEmployeeByEmployeeId(employeeId);

            if (!employeeService.employeeExists(employeeId)) {
                logger.warn("Login failed: Employee ID {} not found", employeeId);
                return ResponseEntity.status(404).body(Map.of(
                        SUCCESS, false,
                        ERROR, "Mitarbeiter ID nicht hinterlegt"
                ));
            }

            if(!employeeOptional.isPresent()) {
                return ResponseEntity.status(404).body(Map.of(
                        SUCCESS, false,
                        ERROR, "Mitarbeiter nicht gefunden"
                ));
            }
            if(employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            if (!authService.verifyPassword(employee.getPassword(), password)) {
                logger.warn("Login failed: Invalid password for employee ID: {}", employeeId);
                return ResponseEntity.status(401).body(Map.of(
                        SUCCESS, false,
                        ERROR, "Falsche Anmeldedaten"
                ));
            }
            }
            logger.info("Login successful for employee ID: {}", employeeId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    SUCCESS, false,
                    ERROR, "Failed to read body: " + e.getMessage()
            ));
        }

    }

    @Operation(summary = "Benutzer-Registrierung", description = "Registriert einen neuen Mitarbeiter mit Mitarbeiter-ID, Passwort, Vorname, Nachname und Rolle.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registrierung erfolgreich"),
            @ApiResponse(responseCode = "400", description = "Mitarbeiter-ID bereits vergeben"),
            @ApiResponse(responseCode = "500", description = "Serverfehler")
    })
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(
            @Parameter(description = "Anmeldedaten des neuen Mitarbeiters", required = true)
            @Valid @RequestBody EmployeeDto signupRequest
    ) {
        try {
            Integer employeeId = signupRequest.getEmployeeId();
            String password = signupRequest.getPassword();
            String firstName = signupRequest.getFirstName();
            String lastName = signupRequest.getLastName();
            Role role = signupRequest.getRole();

            logger.info("Registration attempt for employee ID: {}, name: {} {}, role: {}",
                    employeeId, firstName, lastName, role);

            if (employeeService.employeeExists(employeeId)) {
                logger.warn("Registration failed: Employee ID {} already exists", employeeId);
                return ResponseEntity.status(400).body(Map.of(
                        SUCCESS, false,
                        ERROR, "User with this employee ID already exists"
                ));
            }

            String hashedPassword = authService.hashPassword(password);
            // Nutzer erstellen und speichern
            employeeService.createEmployee(employeeId, firstName, lastName, hashedPassword, role);

            return ResponseEntity.ok(Map.of(SUCCESS, true));
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    SUCCESS, false,
                    ERROR, "Server error occurred" + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Benutzer abmelden", description = "Löscht das JWT-Cookie zur Abmeldung des Benutzers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout erfolgreich"),
            @ApiResponse(responseCode = "500", description = "Logout fehlgeschlagen")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        try {
            Cookie cookie = new Cookie(TOKEN, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed: " + e.getMessage());
        }
    }

    @Operation(summary = "JWT-Cookie validieren", description = "Überprüft, ob ein gültiger Token-Cookie vorhanden ist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cookie gültig"),
            @ApiResponse(responseCode = "204", description = "Kein gültiger Cookie gefunden"),
            @ApiResponse(responseCode = "400", description = "Kein Cookie vorhanden")
    })
    @PostMapping("/cookie-validation")
    public ResponseEntity<String> cookieValidation(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            logger.warn("Cookie validation failed: No cookies found in request");
            return ResponseEntity.badRequest().build();
        }

        try {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(TOKEN))
                    .findFirst()
                    .map(cookie -> {
                        try {
                            if (jwtService.validateToken(cookie.getValue())) {
                                Integer employeeId = jwtService.extractEmployeeId(cookie.getValue());
                                Optional<EmployeeDto> employeeDto = employeeService.getEmployeeById(employeeId);

                                if (employeeDto.isEmpty()) {
                                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
                                }

                                EmployeeDto employee = employeeDto.get();
                                List<String> accessRights = employee.getRolePermissions();
                                String json = "{\"accessRights\": " + new ObjectMapper().writeValueAsString(accessRights) + "}";
                                return ResponseEntity.ok().body(json);
                            } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
                            }
                        } catch (Exception e) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
                        }
                    })
                    .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Validation failed: " + e.getMessage());
        }
    }
}
