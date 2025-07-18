package ip.project.backend.backend.controller;

import ip.project.backend.backend.mapper.RoleMapper;
import ip.project.backend.backend.mapper.UserMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.AuthService;
import ip.project.backend.backend.service.EmployeeService;
import ip.project.backend.backend.service.JwtService;
import ip.project.backend.backend.service.RoleService;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Successful() {
        Employee request = new Employee();
        request.setEmployeeId(1);
        request.setPassword("password");

        Employee storedEmployee = new Employee();
        storedEmployee.setEmployeeId(1);
        storedEmployee.setPassword("hashedpassword");

        when(jwtService.generateToken(1)).thenReturn("fake-jwt");
        when(employeeRepository.findEmployeeByEmployeeId(1)).thenReturn(Optional.of(storedEmployee));
        when(employeeService.employeeExists(1)).thenReturn(true);
        when(authService.verifyPassword("hashedpassword", "password")).thenReturn(true);

        ResponseEntity<Map<String, Object>> result = authController.login(UserMapper.INSTANCE.employeeToEmployeeDto(request), response);

        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void login_InvalidCredentials() {
        Employee request = new Employee();
        request.setEmployeeId(1);
        request.setPassword("wrong");

        Employee storedEmployee = new Employee();
        storedEmployee.setEmployeeId(1);
        storedEmployee.setPassword("hashedpassword");

        when(jwtService.generateToken(1)).thenReturn("token");
        when(employeeRepository.findEmployeeByEmployeeId(1)).thenReturn(Optional.of(storedEmployee));
        when(employeeService.employeeExists(1)).thenReturn(true);
        when(authService.verifyPassword("hashedpassword", "wrong")).thenReturn(false);

        ResponseEntity<Map<String, Object>> result = authController.login(UserMapper.INSTANCE.employeeToEmployeeDto(request), response);

        assertEquals(401, result.getStatusCodeValue());
        assertTrue(result.getBody().containsKey("error"));
    }

    @Test
    void signup_Successful() {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(2);
        dto.setPassword("pass123");
        dto.setFirstName("Max");
        dto.setLastName("Mustermann");
        Role role = new Role();
        dto.setRole(role);

        when(employeeService.employeeExists(2)).thenReturn(false);
        when(authService.hashPassword("pass123")).thenReturn("hashed");
        when(roleService.getRoleByRoleId(any())).thenReturn(Optional.of(RoleMapper.INSTANCE.roleToRoleDto(role)));

        ResponseEntity<Map<String, Object>> result = authController.signup(dto);

        verify(employeeService).createEmployee(2, "Max", "Mustermann", "hashed", role);
        assertEquals(200, result.getStatusCodeValue());
        assertTrue((Boolean) result.getBody().get("success"));
    }

    @Test
    void signup_AlreadyExists() {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(2);
        when(employeeService.employeeExists(2)).thenReturn(true);

        ResponseEntity<Map<String, Object>> result = authController.signup(dto);

        assertEquals(400, result.getStatusCodeValue());
        assertTrue(result.getBody().containsKey("error"));
    }
}
