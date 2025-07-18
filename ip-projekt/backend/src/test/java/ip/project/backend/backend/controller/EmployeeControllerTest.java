package ip.project.backend.backend.controller;

import ip.project.backend.backend.mapper.RoleMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.service.EmployeeService;
import ip.project.backend.backend.service.JwtService;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEmployee_Success() {
        Integer employeeId = 1;
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmployeeId(employeeId);

        when(employeeRepository.findEmployeeByEmployeeId(employeeId)).thenReturn(Optional.of(employee));
        when(employeeService.employeeToDto(employee)).thenReturn(employeeDto);

        ResponseEntity<EmployeeDto> response = employeeController.getEmployee(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(employeeId, response.getBody().getEmployeeId());
    }

    @Test
    void testGetEmployee_NotFound() {
        Integer employeeId = 1;
        when(employeeRepository.findEmployeeByEmployeeId(employeeId)).thenReturn(Optional.empty());

        ResponseEntity<EmployeeDto> response = employeeController.getEmployee(employeeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetAllEmployees_Success() {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmployeeId(1);
        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeDto));

        ResponseEntity<List<EmployeeDto>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllEmployees_NoContent() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        ResponseEntity<List<EmployeeDto>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

@Test
void testUpdateEmployee_Success() {
    EmployeeDto employeeDto = new EmployeeDto();
    employeeDto.setEmployeeId(1);
    employeeDto.setFirstName("John");
    employeeDto.setLastName("Doe");
    // Dummy-RoleDto setzen, um NullPointer zu vermeiden
    Role dummyRole = new Role();
    dummyRole.setRoleId(1);
    employeeDto.setRole(dummyRole);

    when(roleService.getRoleByRoleId(anyInt())).thenReturn(Optional.of(RoleMapper.INSTANCE.roleToRoleDto(dummyRole)));
    when(employeeService.updateEmployee(any(EmployeeDto.class))).thenReturn(Optional.empty());

    ResponseEntity<String> response = employeeController.updateEmployee(employeeDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Employee updated successfully", response.getBody());
}

@Test
void testUpdateEmployee_Failure() {
    EmployeeDto employeeDto = new EmployeeDto();
    employeeDto.setEmployeeId(1);
    employeeDto.setFirstName("John");
    employeeDto.setLastName("Doe");
    // Dummy-RoleDto setzen, um NullPointer zu vermeiden
    Role dummyRole = new Role();
    dummyRole.setRoleId(1);
    employeeDto.setRole(dummyRole);

    when(roleService.getRoleByRoleId(anyInt())).thenReturn(Optional.of(RoleMapper.INSTANCE.roleToRoleDto(dummyRole)));

    String errorMessage = "Failed to update employee";
    when(employeeService.updateEmployee(any(EmployeeDto.class))).thenReturn(Optional.of(errorMessage));

    ResponseEntity<String> response = employeeController.updateEmployee(employeeDto);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(errorMessage, response.getBody());
}

    @Test
    void testDeleteEmployee_Success() {
        Integer employeeId = 1;
        String token = "token";
        when(jwtService.extractEmployeeId(token)).thenReturn(2);
        when(employeeService.deleteEmployee(employeeId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployee(token, employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

    @Test
    void testDeleteEmployee_Failure() {
        Integer employeeId = 1;
        String errorMessage = "Failed to delete employee";
        String token = "token";

        when(jwtService.extractEmployeeId(token)).thenReturn(2);
        when(employeeService.deleteEmployee(employeeId)).thenReturn(Optional.of(errorMessage));

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployee(token, employeeId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void testDeleteEmployee_BadRequest_NoId() {
        // Arrange
        String token = "token";
        when(jwtService.extractEmployeeId(token)).thenReturn(2);
        when(employeeService.deleteEmployee(null)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployee(token, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());

        verify(employeeService).deleteEmployee(null);
    }

    @Test
    void testDeleteEmployee_SelfDeletion_ShouldReturnForbidden() {
        Integer employeeId = 123;
        String token = "self-token";

        when(jwtService.extractEmployeeId(token)).thenReturn(employeeId);      // Selbst-Löschung

        ResponseEntity<String> response = employeeController.deleteEmployee(token, employeeId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You cannot delete yourself", response.getBody());
        verify(employeeService, never()).deleteEmployee(any());
    }

    @Test
    void testDeleteEmployee_DifferentUser_ShouldSucceed() {
        Integer employeeToDelete = 456;
        Integer currentUserId = 123;
        String token = "valid-token";

        when(jwtService.extractEmployeeId(token)).thenReturn(currentUserId);   // anderer User
        when(employeeService.deleteEmployee(employeeToDelete)).thenReturn(Optional.empty());

        ResponseEntity<String> response = employeeController.deleteEmployee(token, employeeToDelete);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        verify(employeeService, times(1)).deleteEmployee(employeeToDelete);
    }

    @Test
    void testDeleteEmployee_NoToken_ShouldReturnUnauthorized() {
        Integer employeeId = 123;

        ResponseEntity<String> response = employeeController.deleteEmployee(null, employeeId);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());
        verify(employeeService, never()).deleteEmployee(any());
    }

    @Test
    void testDeleteEmployee_InvalidToken_ShouldReturnUnauthorized() {
        Integer employeeId = 123;
        String invalidToken = "invalid-token";

        when(jwtService.extractEmployeeId(invalidToken)).thenReturn(null);

        ResponseEntity<String> response = employeeController.deleteEmployee(invalidToken, employeeId);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid authentication token", response.getBody());
        verify(employeeService, never()).deleteEmployee(any());
    }
}
