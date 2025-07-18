package ip.project.backend.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        // Setup a basic Employee object for testing
        employee = new Employee(123, "John", "Doe", "password123", new Role());
    }

    @Test
    void testGetEmployeeId() {
        assertEquals(123, employee.getEmployeeId(), "Employee ID should match");
    }

    @Test
    void testSetEmployeeId() {
        employee.setEmployeeId(456);
        assertEquals(456, employee.getEmployeeId(), "Employee ID should be updated");
    }

    @Test
    void testGetFirstName() {
        assertEquals("John", employee.getFirstName(), "First name should match");
    }

    @Test
    void testSetFirstName() {
        employee.setFirstName("Jane");
        assertEquals("Jane", employee.getFirstName(), "First name should be updated");
    }

    @Test
    void testGetLastName() {
        assertEquals("Doe", employee.getLastName(), "Last name should match");
    }

    @Test
    void testSetLastName() {
        employee.setLastName("Smith");
        assertEquals("Smith", employee.getLastName(), "Last name should be updated");
    }

    @Test
    void testGetPassword() {
        assertEquals("password123", employee.getPassword(), "Password should match");
    }

    @Test
    void testSetPassword() {
        employee.setPassword("newpassword");
        assertEquals("newpassword", employee.getPassword(), "Password should be updated");
    }

    @Test
    void testGetRole() {
        Role role = new Role();
        employee.setRole(role);
        assertEquals(role, employee.getRole(), "Role should match");
    }

    @Test
    void testSetRole() {
        Role role = new Role();
        employee.setRole(role);
        assertEquals(role, employee.getRole(), "Role should be updated");
    }

    @Test
    void testGetRolePermissions() {
        Role role = new Role();
        role.setRolePermissions(List.of("VIEW", "EDIT"));
        employee.setRole(role);

        List<String> permissions = employee.getRolePermissions();
        assertNotNull(permissions, "Permissions list should not be null");
        assertEquals(2, permissions.size(), "Permissions list should have two items");
        assertTrue(permissions.contains("VIEW"), "Permissions should contain VIEW");
        assertTrue(permissions.contains("EDIT"), "Permissions should contain EDIT");
    }

    @Test
    void testGetRolePermissionsWhenRoleIsNull() {
        employee.setRole(null);
        List<String> permissions = employee.getRolePermissions();
        assertNotNull(permissions, "Permissions list should not be null when role is null");
        assertTrue(permissions.isEmpty(), "Permissions list should be empty when role is null");
    }
}
