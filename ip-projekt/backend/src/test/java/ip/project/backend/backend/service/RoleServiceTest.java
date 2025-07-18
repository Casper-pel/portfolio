package ip.project.backend.backend.service;

import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private RoleService roleService;

    private Role exampleRole;
    private RoleDto exampleDto;
    private Employee emp1;
    private Employee emp2;
    private EmployeeDto empDto1;
    private EmployeeDto empDto2;

    @BeforeEach
    void setUp() {
        // Beispiel-Rolle ohne Employee-IDs
        exampleRole = new Role();
        exampleRole.setRoleId(42);
        exampleRole.setRoleName("Admin");
        exampleRole.setDescription("Administratorrolle");
        exampleRole.setRolePermissions(List.of("READ", "WRITE"));

        // RoleDto ohne Employee-IDs im Konstruktor
        exampleDto = new RoleDto(
                42,
                "Admin",
                "Administratorrolle",
                List.of("READ", "WRITE")
        );

        // Beispiel-Mitarbeiter mit zugewiesener Rolle
        emp1 = new Employee();
        emp1.setEmployeeId(1);
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        emp1.setRole(exampleRole);

        emp2 = new Employee();
        emp2.setEmployeeId(2);
        emp2.setFirstName("Jane");
        emp2.setLastName("Smith");
        emp2.setRole(exampleRole);

        // EmployeeDtos
        empDto1 = new EmployeeDto();
        empDto1.setEmployeeId(1);
        empDto1.setFirstName("John");
        empDto1.setLastName("Doe");
        empDto1.setPassword(null);
        empDto2 = new EmployeeDto();
        empDto2.setEmployeeId(2);
        empDto2.setFirstName("Jane");
        empDto2.setLastName("Smith");
        empDto2.setPassword(null);
    }

    @Test
    void getRoleByRoleId_Found() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));
        when(employeeRepository.findByRole_RoleId(42)).thenReturn(List.of(emp1, emp2));
        when(employeeService.employeeToDto(emp1)).thenReturn(empDto1);
        when(employeeService.employeeToDto(emp2)).thenReturn(empDto2);

        // Act
        Optional<RoleDto> result = roleService.getRoleByRoleId(42);

        // Assert
        assertTrue(result.isPresent());
        RoleDto roleDto = result.get();
        assertEquals(42, roleDto.getRoleId());
        assertEquals("Admin", roleDto.getRoleName());
        assertEquals("Administratorrolle", roleDto.getDescription());
        assertEquals(List.of("READ", "WRITE"), roleDto.getRolePermissions());
        assertEquals(2, roleDto.getEmployeeDtos().size());
        assertEquals("John", roleDto.getEmployeeDtos().get(0).getFirstName());
        assertEquals("Jane", roleDto.getEmployeeDtos().get(1).getFirstName());

        verify(roleRepository).findRoleByRoleId(42);
        verify(employeeRepository).findByRole_RoleId(42);
        verify(employeeService).employeeToDto(emp1);
        verify(employeeService).employeeToDto(emp2);
    }

    @Test
    void getRoleByRoleId_NotFound() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.empty());

        // Act
        Optional<RoleDto> result = roleService.getRoleByRoleId(42);

        // Assert
        assertFalse(result.isPresent());

        verify(roleRepository).findRoleByRoleId(42);
        verifyNoInteractions(employeeRepository);
        verifyNoInteractions(employeeService);
    }

    @Test
    void getRoleByRoleId_NoEmployees() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));
        when(employeeRepository.findByRole_RoleId(42)).thenReturn(new ArrayList<>());

        // Act
        Optional<RoleDto> result = roleService.getRoleByRoleId(42);

        // Assert
        assertTrue(result.isPresent());
        RoleDto roleDto = result.get();
        assertEquals(42, roleDto.getRoleId());
        assertEquals(0, roleDto.getEmployeeDtos().size());

        verify(roleRepository).findRoleByRoleId(42);
        verify(employeeRepository).findByRole_RoleId(42);
        verifyNoInteractions(employeeService);
    }

    @Test
    void getAllRoles_Found() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(List.of(exampleRole));
        when(employeeRepository.findByRole_RoleId(42)).thenReturn(List.of(emp1));
        when(employeeService.employeeToDto(emp1)).thenReturn(empDto1);

        // Act
        List<RoleDto> result = roleService.getAllRoles();

        // Assert
        assertEquals(1, result.size());
        RoleDto roleDto = result.get(0);
        assertEquals(42, roleDto.getRoleId());
        assertEquals("Admin", roleDto.getRoleName());
        assertEquals(1, roleDto.getEmployeeDtos().size());

        verify(roleRepository).findAll();
        verify(employeeRepository).findByRole_RoleId(42);
        verify(employeeService).employeeToDto(emp1);
    }

    @Test
    void getAllRoles_Empty() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<RoleDto> result = roleService.getAllRoles();

        // Assert
        assertEquals(0, result.size());

        verify(roleRepository).findAll();
        verifyNoInteractions(employeeRepository);
        verifyNoInteractions(employeeService);
    }

    @Test
    void addRole_Success() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(exampleRole);

        // Act
        Optional<String> result = roleService.addRole(exampleDto);

        // Assert
        assertFalse(result.isPresent());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void addRole_AlreadyExists() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));

        // Act
        Optional<String> result = roleService.addRole(exampleDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Role already exists with role id: 42", result.get());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_Success() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));
        when(roleRepository.save(any(Role.class))).thenReturn(exampleRole);

        // Act
        Optional<String> result = roleService.updateRole(exampleDto);

        // Assert
        assertFalse(result.isPresent());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void updateRole_NotFound() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.empty());

        // Act
        Optional<String> result = roleService.updateRole(exampleDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Role not found with role id: 42", result.get());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void deleteRole_Success() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));

        // Act
        Optional<String> result = roleService.deleteRole("42");

        // Assert
        assertFalse(result.isPresent());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository).delete(exampleRole);
    }

    @Test
    void deleteRole_NotFound() {
        // Arrange
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.empty());

        // Act
        Optional<String> result = roleService.deleteRole("42");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Role not found with role id: 42", result.get());

        verify(roleRepository).findRoleByRoleId(42);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void roleToDto_WithEmployees() {
        // Arrange
        when(employeeRepository.findByRole_RoleId(42)).thenReturn(List.of(emp1, emp2));
        when(employeeService.employeeToDto(emp1)).thenReturn(empDto1);
        when(employeeService.employeeToDto(emp2)).thenReturn(empDto2);

        // Act - Verwendung der package-private Methode über einen Public-Aufruf
        when(roleRepository.findRoleByRoleId(42)).thenReturn(Optional.of(exampleRole));
        Optional<RoleDto> result = roleService.getRoleByRoleId(42);

        // Assert
        assertTrue(result.isPresent());
        RoleDto roleDto = result.get();
        assertEquals(2, roleDto.getEmployeeDtos().size());
        assertEquals("John", roleDto.getEmployeeDtos().get(0).getFirstName());
        assertEquals("Jane", roleDto.getEmployeeDtos().get(1).getFirstName());

        verify(employeeRepository).findByRole_RoleId(42);
        verify(employeeService).employeeToDto(emp1);
        verify(employeeService).employeeToDto(emp2);
    }
}
