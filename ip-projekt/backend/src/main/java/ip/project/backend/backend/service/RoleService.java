package ip.project.backend.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ip.project.backend.backend.mapper.RoleMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.RoleRepository;

@Service
public class RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @Autowired
    public RoleService(RoleRepository roleRepository, EmployeeRepository employeeRepository, EmployeeService employeeService) {
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    /**
     * Retrieves a role by its ID and converts it to a DTO.
     *
     * @param roleId The ID of the role to retrieve
     * @return An Optional containing the RoleDto if found, or empty if not found
     */
    public Optional<RoleDto> getRoleByRoleId(Integer roleId) {
        Optional<Role> role = roleRepository.findRoleByRoleId(roleId);
        if (role.isEmpty()) {
            logger.error("Role with id {} not found", roleId);
            return Optional.empty();
        } else {
            return Optional.of(roleToDto(role.get()));
        }
    }

    /**
     * Retrieves all roles and converts them to DTOs.
     *
     * @return A list of RoleDto objects representing all roles
     */
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> roleDtos = new ArrayList<>();
        if (roles.isEmpty()) {
            logger.info("No roles found");
            return roleDtos;
        } else {
            for (Role role : roles) {
                roleDtos.add(roleToDto(role));
            }
            return roleDtos;
        }
    }

    /**
     * Converts a Role entity to a RoleDto.
     *
     * @param role The Role entity to convert
     * @return The converted RoleDto
     */
    RoleDto roleToDto(Role role) {
        logger.debug("Converting Role {} to DTO", role.getRoleId());

        // Finde alle Mitarbeiter mit dieser Rolle
        List<Employee> employeesWithRole = employeeRepository.findByRole_RoleId(role.getRoleId());
        List<EmployeeDto> employeeDtos = new ArrayList<>();

        if (employeesWithRole != null && !employeesWithRole.isEmpty()) {
            logger.info("Role {} has {} employees assigned", role.getRoleId(), employeesWithRole.size());

            for (Employee employee : employeesWithRole) {
                EmployeeDto employeeDto = employeeService.employeeToDto(employee);
                employeeDtos.add(employeeDto);
                logger.debug("Successfully mapped employee {} to DTO", employee.getEmployeeId());
            }

            logger.info("Successfully mapped {} employees for role {}", employeeDtos.size(), role.getRoleId());
        } else {
            logger.info("Role {} has no employees assigned", role.getRoleId());
        }

        // Erstelle RoleDto ohne Employee-IDs
        RoleDto roleDto = new RoleDto(role.getRoleId(), role.getRoleName(), role.getDescription(), role.getRolePermissions());

        // Setze die vollständigen Employee-Objekte
        roleDto.setEmployeeDtos(employeeDtos);
        logger.debug("Role DTO created with {} employee DTOs", employeeDtos.size());

        return roleDto;
    }

    /**
     * Adds a new role to the database.
     *
     * @param roleDto The RoleDto object containing the role details
     * @return An Optional containing an error message if the role already exists, or empty if added successfully
     */
    public Optional<String> addRole(RoleDto roleDto) {
        if (roleRepository.findRoleByRoleId(roleDto.getRoleId()).isPresent()) {
            logger.error("Role with id {} already exists", roleDto.getRoleId());
            return Optional.of("Role already exists with role id: " + roleDto.getRoleId());
        }

        Role role = RoleMapper.INSTANCE.roleDtoToRole(roleDto);
        roleRepository.save(role);

        logger.info("Role with id {} added", role.getRoleId());
        return Optional.empty();
    }

    /**
     * Updates an existing role in the database.
     *
     * @param roleDto The RoleDto object containing the updated role details
     * @return An Optional containing an error message if the role does not exist, or empty if updated successfully
     */
    public Optional<String> updateRole(RoleDto roleDto) {
        Optional<Role> roleOpt = roleRepository.findRoleByRoleId(roleDto.getRoleId());
        if (roleOpt.isEmpty()) {
            logger.error("Role with id {} not found", roleDto.getRoleId());
            return Optional.of("Role not found with role id: " + roleDto.getRoleId());
        }

        Role role = roleOpt.get();
        role.setRoleName(roleDto.getRoleName());
        role.setDescription(roleDto.getDescription());
        role.setRolePermissions(roleDto.getRolePermissions());

        roleRepository.save(role);

        logger.info("Role with id {} updated", role.getRoleId());
        return Optional.empty();
    }

    /**
     * Deletes a role from the database.
     *
     * @param roleId The ID of the role to delete
     * @return An Optional containing an error message if the role does not exist, or empty if deleted successfully
     */
    public Optional<String> deleteRole(String roleId) {
        Optional<Role> role = roleRepository.findRoleByRoleId(Integer.parseInt(roleId));
        if (role.isEmpty()) {
            logger.error("Role with id {} not found", roleId);
            return Optional.of("Role not found with role id: " + roleId);
        }
        roleRepository.delete(role.get());
        List<Employee> employeesWithRoleToDelete = employeeRepository.findByRole_RoleId(Integer.parseInt(roleId));
        if (employeesWithRoleToDelete != null && !employeesWithRoleToDelete.isEmpty()) {
            logger.info("Deleting {} employees assigned to role {}", employeesWithRoleToDelete.size(), roleId);
            for (Employee employee : employeesWithRoleToDelete) {
                employee.setRole(null);
                employeeRepository.save(employee);
            }
        } else {
            logger.info("No employees found with role id {}", roleId);
        }
        logger.info("Role with id {} deleted", role.get().getRoleId());
        return Optional.empty();
    }
}
