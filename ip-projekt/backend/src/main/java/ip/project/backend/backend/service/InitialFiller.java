package ip.project.backend.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.RoleRepository;
import jakarta.annotation.PostConstruct;

@Component
public class InitialFiller {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final Logger logger = LoggerFactory.getLogger(InitialFiller.class);
    private final RoleService roleService;

    @Autowired
    InitialFiller(EmployeeRepository employeeRepository, RoleRepository roleRepository, AuthService authService, RoleService roleService) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
        this.roleService = roleService;
    }

    @PostConstruct
    public void fillInitialData() {
        if (!employeeRepository.findAll().isEmpty()) return;

        logger.info("Database is empty, creating initial admin user and role...");

        try {
            // Generate a unique roleId (start with 1 since it's the first role)
            Integer adminRoleId = 1;
            List<String> permissions = new ArrayList<>();
            permissions.add("admin");
            
            // Check if roleId already exists and increment if needed
            while (roleRepository.findRoleByRoleId(adminRoleId).isPresent()) {
                adminRoleId++;
            }

            // Create admin role using RoleDto and RoleService for proper handling
            RoleDto adminRoleDto = new RoleDto();
            adminRoleDto.setRoleName("Admin");
            adminRoleDto.setRoleId(adminRoleId);
            adminRoleDto.setDescription("Administrator role with full access");
            adminRoleDto.setRolePermissions(permissions);
            adminRoleDto.setEmployeeDtos(new ArrayList<>());

            // Add the role using RoleService
            Optional<String> roleResult = roleService.addRole(adminRoleDto);
            if (roleResult.isPresent()) {
                logger.error("Failed to create admin role: {}", roleResult.get());
                return;
            }

            // Retrieve the saved role to get the complete object
            Optional<Role> savedAdminRoleOpt = roleRepository.findRoleByRoleId(adminRoleId);
            if (savedAdminRoleOpt.isEmpty()) {
                logger.error("Failed to retrieve saved admin role");
                return;
            }
            
            Role savedAdminRole = savedAdminRoleOpt.get();
            logger.info("Created admin role with ID: {} and roleId: {}", savedAdminRole.get_id(), savedAdminRole.getRoleId());

            // Create initial admin user with reference to the saved role
            Employee adminEmployee = new Employee(1, "Admin", "Admin", authService.hashPassword("admin"), savedAdminRole);
            Employee savedEmployee = employeeRepository.save(adminEmployee);
            
            logger.info("Created admin user with employeeId: {} and role: {}", savedEmployee.getEmployeeId(), savedAdminRole.getRoleName());
            
            // Update the role to include this employee in its employee list
            roleRepository.save(savedAdminRole);
            
            logger.info("Initial data setup completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during initial data setup: {}", e.getMessage(), e);
        }
    }
}
