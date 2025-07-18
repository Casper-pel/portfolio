package ip.project.backend.backend.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.PasswordUpdateRequest;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.EmployeeService;
import ip.project.backend.backend.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/employee")

public class EmployeeController {
    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository, JwtService jwtService) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Get current user", description = "Get current logged-in user from JWT token")
    @ApiResponse(responseCode = "200", description = "Current user found")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/current")
    public ResponseEntity<EmployeeDto> getCurrentEmployee(HttpServletRequest request) {
        // Extract token from cookies
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            logger.error("Authentication failed: No token found in request cookies");
            return ResponseEntity.status(401).build();
        }

        // Extract employee ID from token
        Integer employeeId = jwtService.extractEmployeeId(token);
        if (employeeId == null) {
            logger.error("Invalid token  -  cannot extract employee ID");
            return ResponseEntity.status(401).build();
        }

        // Get employee by ID
        Optional<Employee> optionalEmployee = employeeRepository.findEmployeeByEmployeeId(employeeId);
        if (optionalEmployee.isEmpty()) {
            logger.error("Employee not found for ID: {}", employeeId);
            return ResponseEntity.status(404).build();
        }

        Employee employee = optionalEmployee.get();
        EmployeeDto employeeDto = employeeService.employeeToDto(employee);
        return ResponseEntity.ok(employeeDto);
    }

    @Operation(summary = "Get user by id", description = "Get user by id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "400", description = "User not found. Error message provided")
    @GetMapping("/get/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable("id") Integer employeeId) {
        if (employeeId == null) {
            logger.error("User id is null");
            return ResponseEntity.badRequest().build();
        }

        Optional<Employee> optionalEmployee = employeeRepository.findEmployeeByEmployeeId(employeeId);

        if (optionalEmployee.isEmpty()) {
            logger.warn("Employee not found for id: {}", employeeId);
            return ResponseEntity.noContent().build();
        } else {
            Employee employee = optionalEmployee.get();
            EmployeeDto employeeDto = employeeService.employeeToDto(employee);
            return ResponseEntity.ok(employeeDto);
        }
    }

    @Operation(summary = "Get all users", description = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users found")
    @ApiResponse(responseCode = "400", description = "No users found. Error message provided")
    @GetMapping("/all")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        if (employees.isEmpty()) {
            logger.info("No employees found in the database");
            return ResponseEntity.noContent().build();
        } else {
            logger.info("Found {} employees in the database", employees.size());
            return ResponseEntity.ok(employees);
        }
    }

    @Operation(summary = "Update user", description = "Update existing User")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "User not updated. Error message provided")
    @PutMapping("/update")
    public ResponseEntity<String> updateEmployee(@Parameter(description = "UserDto Object to update into the databse", required = true) @NotNull @Valid @RequestBody EmployeeDto employeeDto) {
        Optional<String> answer = employeeService.updateEmployee(employeeDto);

        if (answer.isEmpty()) {
            return ResponseEntity.ok("Employee updated successfully");
        } else {
            logger.error("Employee with ID: {} not updated. Error message: {}", employeeDto.getEmployeeId(), answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Update user password", description = "Update password for current user")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @ApiResponse(responseCode = "400", description = "Password not updated. Error message provided")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @CookieValue(value = "token", required = false) String token,
            @Parameter(description = "Password update request", required = true)
            @NotNull @Valid @RequestBody PasswordUpdateRequest passwordRequest) {

        if (token == null) {
            logger.error("No authentication token found");
            return ResponseEntity.status(401).body("Authentication required");
        }

        Integer currentUserId = jwtService.extractEmployeeId(token);
        if (currentUserId == null) {
            logger.error("Invalid token - cannot extract employee ID");
            return ResponseEntity.status(401).body("Invalid authentication token");
        }

        Optional<String> answer = employeeService.updatePassword(
            currentUserId,
            passwordRequest.getOldPassword(),
            passwordRequest.getNewPassword()
        );

        if (answer.isEmpty()) {
            logger.info("Password updated successfully for user {}", currentUserId);
            return ResponseEntity.ok("Password updated successfully");
        } else {
            logger.error("Password not updated. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Delete employee", description = "Delete existing User")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "400", description = "User not deleted. Error message provided")
    @ApiResponse(responseCode = "403", description = "Cannot delete yourself")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEmployee(
            @CookieValue(value = "token", required = false) String token,
            @Parameter(description = "Employee Id of the employee to delete", required = true, example = "1") 
            @NotNull @PathVariable("id") Integer employeeId
        ) {
        if (token == null) {
            logger.error("No authentication token found");
            return ResponseEntity.status(401).body("Authentication required");
        }

        // Extract current user's ID from token
        Integer currentUserId = jwtService.extractEmployeeId(token);
        if (currentUserId == null) {
            logger.error("Invalid token - cannot extract employee ID");
            return ResponseEntity.status(401).body("Invalid authentication token");
        }

        // Check if user is trying to delete themselves
        if (currentUserId.equals(employeeId)) {
            logger.warn("Employee {} attempted to delete themselves", currentUserId);
            return ResponseEntity.status(403).body("You cannot delete yourself");
        }

        Optional<String> answer = employeeService.deleteEmployee(employeeId);

        if (answer.isEmpty()) {
            logger.info("User {} deleted successfully by user {}", employeeId, currentUserId);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            logger.error("User not deleted. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }
}
