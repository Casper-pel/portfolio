package ip.project.backend.backend.service;

import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing employee-related operations. This service handles CRUD
 * operations for employees, including creating, retrieving, updating, and
 * deleting employee records. It also manages the relationship between employees
 * and their roles.
 */
@Service
public class EmployeeService {

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final UrlaubsAntragService urlaubsAntragService;
    private final AuthService authService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, UrlaubsAntragService urlaubsAntragService, AuthService authService) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.urlaubsAntragService = urlaubsAntragService;
        this.authService = authService;
    }

    /**
     * Checks if an employee with the given ID exists in the database.
     *
     * @param id The employee ID to check
     * @return true if the employee exists, false otherwise
     */
    public boolean employeeExists(Integer id) {
        logger.debug("Checking if employee with ID {} exists", id);
        return employeeRepository.findEmployeeByEmployeeId(id).isPresent();
    }

    /**
     * Creates a new employee with the given information. If a role is provided,
     * it fetches the persisted version from the database.
     *
     * @param id        The employee ID
     * @param firstName The employee's first name
     * @param lastName  The employee's last name
     * @param password  The employee's password
     * @param role      The employee's role
     * @return The created employee entity
     */
    public Employee createEmployee(Integer id, String firstName, String lastName, String password, Role role) {
        logger.info("Creating new employee with ID: {}, name: {} {}", id, firstName, lastName);

        Role persistedRole = null;
        if (role != null && role.getRoleId() != null) {
            logger.debug("Fetching role with ID: {} from database", role.getRoleId());
            Optional<Role> existingRole = roleRepository.findRoleByRoleId(role.getRoleId());
            persistedRole = existingRole.orElse(null);
            if (existingRole.isEmpty()) {
                logger.warn("Role with ID {} not found, setting role to null", role.getRoleId());
            }
        }

        Employee employee = new Employee(id, firstName, lastName, password, persistedRole);
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee with ID: {} created successfully", id);
        return savedEmployee;
    }

    /**
     * Retrieves all employees from the database and converts them to DTOs.
     *
     * @return A list of employee DTOs
     */
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDto> employeeDtos = new ArrayList<>();
        if (employees.isEmpty()) {
            logger.info("No users found");
            return employeeDtos;
        } else {
            for (Employee employee : employees) {
                try {
                    employeeDtos.add(employeeToDto(employee));
                } catch (Exception e) {
                    logger.error("Error converting employee {} to DTO: {}", employee.getEmployeeId(), e.getMessage());
                }
            }
            return employeeDtos;
        }
    }

    /**
     * Converts an Employee entity to an EmployeeDto
     *
     * @param employee The Employee entity to convert
     * @return The converted EmployeeDto
     */
    public EmployeeDto employeeToDto(Employee employee) {
        Role safeRole = employee.getRole();
        if (safeRole != null) {
            Role detachedRole = new Role();
            detachedRole.setRoleId(safeRole.getRoleId());
            detachedRole.setRoleName(safeRole.getRoleName());
            detachedRole.setDescription(safeRole.getDescription());
            // Kopiere die RolePermissions
            if (safeRole.getRolePermissions() != null) {
                detachedRole.setRolePermissions(new ArrayList<>(safeRole.getRolePermissions()));
            } else {
                detachedRole.setRolePermissions(new ArrayList<>());
            }
            safeRole = detachedRole;
        }
        return new EmployeeDto(employee.getEmployeeId(), employee.getFirstName(), employee.getLastName(), employee.getPassword(), safeRole);
    }

    /**
     * Retrieves an employee by their ID and converts it to a DTO.
     *
     * @param employeeId The ID of the employee to retrieve
     * @return An Optional containing the EmployeeDto if found, or empty if not found
     */
    public Optional<EmployeeDto> getEmployeeById(Integer employeeId) {
        Optional<Employee> employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            logger.error("User with id {} not found", employeeId);
            return Optional.empty();
        } else {
            return Optional.of(employeeToDto(employee.get()));
        }
    }

    /**
     * Updates an existing employee with the information provided in the DTO.
     * This method updates the employee's first name, last name, password (if
     * provided), and role (if provided).
     *
     * @param employeeDto The DTO containing the updated employee information
     * @return An empty Optional if the update was successful, or an Optional
     * containing an error message if the employee was not found
     */
    public Optional<String> updateEmployee(EmployeeDto employeeDto) {
        logger.info("Updating employee with ID: {}", employeeDto.getEmployeeId());

        Optional<Employee> employeeOpt = employeeRepository.findEmployeeByEmployeeId(employeeDto.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            logger.error("User with id {} not found", employeeDto.getEmployeeId());
            return Optional.of("User not found with user id: " + employeeDto.getEmployeeId());
        }

        Employee employee = employeeOpt.get();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());

        if (employeeDto.getPassword() != null && !employeeDto.getPassword().trim().isEmpty()) {
            employee.setPassword(employeeDto.getPassword());
        }

        Role persistedRole = null;
        Integer newRoleId = null;
        if (employeeDto.getRole() != null && employeeDto.getRole().getRoleId() != null) {
            newRoleId = employeeDto.getRole().getRoleId();
            Optional<Role> existingRole = roleRepository.findRoleByRoleId(newRoleId);
            persistedRole = existingRole.orElse(null);

            if (existingRole.isEmpty()) {
                logger.warn("Role with ID {} not found, setting role to null", newRoleId);
            }
        }

        employee.setRole(persistedRole);
        employeeRepository.save(employee);

        return Optional.empty();
    }

    /**
     * Deletes an employee with the given ID. This method also deletes all
     * vacation requests associated with the employee.
     *
     * @param employeeId The ID of the employee to delete
     * @return An empty Optional if the deletion was successful, or an Optional
     * containing an error message if the employee was not found
     */
    public Optional<String> deleteEmployee(Integer employeeId) {
        logger.info("Deleting employee with ID: {}", employeeId);

        Optional<Employee> employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            logger.error("User with id {} not found", employeeId);
            return Optional.of("User not found with user id: " + employeeId);
        }

        logger.debug("Deleting all vacation requests for employee with ID: {}", employeeId);
        urlaubsAntragService.deleteAllUrlaubsAntraegeByEmployeeId(employeeId);

        logger.debug("Deleting employee with ID: {}", employeeId);
        employeeRepository.delete(employee.get());
        logger.info("User with id {} deleted along with all their vacation requests", employee.get().getEmployeeId());
        return Optional.empty();
    }

    /**
     * Updates the password for an employee. This method first verifies the old
     * @param employeeId ID of the employee whose password is being updated
     * @param oldPassword The current password of the employee
     * @param newPassword The new password to set for the employee
     * @return An Optional containing an error message if the update fails, or empty if successful
     */
    public Optional<String> updatePassword(Integer employeeId, String oldPassword, String newPassword) {
        logger.info("Updating password for employee with ID: {}", employeeId);

        Optional<Employee> employeeOpt = employeeRepository.findEmployeeByEmployeeId(employeeId);
        if (employeeOpt.isEmpty()) {
            logger.error("User with id {} not found", employeeId);
            return Optional.of("User not found with user id: " + employeeId);
        }

        Employee employee = employeeOpt.get();

        // Erst das alte Passwort verifizieren
        Boolean validPassword = authService.verifyPassword(employee.getPassword(), oldPassword);
        if (!validPassword) {
            logger.error("Old password does not match for employee with ID: {}", employeeId);
            return Optional.of("Old password does not match");
        }

        // Dann das neue Passwort setzen
        employee.setPassword(authService.hashPassword(newPassword));
        employeeRepository.save(employee);

        logger.info("Password updated successfully for employee with ID: {}", employeeId);
        return Optional.empty();
    }
}
