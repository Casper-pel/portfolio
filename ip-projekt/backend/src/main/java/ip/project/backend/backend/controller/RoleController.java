package ip.project.backend.backend.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.mapper.RoleMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    private final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roleService;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get role", description = "Get existing Role from roles")
    @ApiResponse(responseCode = "200", description = "Role found")
    @ApiResponse(responseCode = "400", description = "Role not found. Error message provided")
    @GetMapping("/get/{id}")
    public ResponseEntity<RoleDto> getRole(@PathVariable("id") Integer roleId) {
        if (roleId == null) {
            logger.error("Role id is null");
            return ResponseEntity.badRequest().build();
        }

        Optional<RoleDto> role = roleService.getRoleByRoleId(roleId);

        if (role.isEmpty()) {
            logger.info("Role not found for id: {}", roleId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(role.get());
        }
    }

    @Operation(summary = "Get all roles", description = "Get all existing Roles from roles")
    @ApiResponse(responseCode = "200", description = "Roles found")
    @ApiResponse(responseCode = "400", description = "Roles not found. Error message provided")
    @GetMapping("/all")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        if (roles.isEmpty()) {
            logger.info("No roles found");
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(roles);
        }
    }

    @Operation(summary = "Add role", description = "Add new Role to existing roles")
    @ApiResponse(responseCode = "200", description = "Role added successfully")
    @ApiResponse(responseCode = "400", description = "Role not added. Error message provided")
    @PostMapping("/add")
    public ResponseEntity<String> addRole(@Parameter(description = "RoleDto Object to add into the databse", required = true) @NotNull @Valid @RequestBody RoleDto roleDto) {
        Optional<String> answer = roleService.addRole(roleDto);

        if (answer.isEmpty()) {
            logger.info("Role {} added successfully", roleDto.getRoleName());
            return ResponseEntity.ok("Role added successfully");
        } else {
            logger.error("Role not added. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Delete role", description = "Delete existing Role from roles")
    @ApiResponse(responseCode = "200", description = "Role deleted successfully")
    @ApiResponse(responseCode = "400", description = "Role not deleted. Error message provided")
    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<String> deleteRole(@Parameter(description = "RoleId of the role to delete", required = true, example = "1") @NotNull @PathVariable("id") String roleId) {
        Integer roleIdInt = Integer.valueOf(roleId);

        // Prüfe erst, ob die Rolle existiert
        Optional<String> answer = roleService.deleteRole(roleId);

        if (answer.isEmpty()) {
            // Nur wenn Löschen erfolgreich war, setze Employees auf null
            List<Employee> employees = employeeRepository.findByRole_RoleId(roleIdInt);
            employees.forEach(employee -> employee.setRole(null));
            employeeRepository.saveAll(employees);

            logger.info("Role with ID {} deleted successfully", roleId);
            return ResponseEntity.ok("Role deleted successfully");
        } else {
            logger.error("Role not deleted. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Update role", description = "Update existing Role from roles")
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    @ApiResponse(responseCode = "400", description = "Role not updated. Error message provided")
    @PutMapping("/update")
    @Transactional
    public ResponseEntity<String> updateRole(@Parameter(description = "RoleDto Object to update into the databse", required = true) @NotNull @Valid @RequestBody RoleDto roleDto) {

        Optional<String> answer = roleService.updateRole(roleDto);

        if (answer.isEmpty()) {
            List<Employee> employees = employeeRepository.findByRole_RoleId(roleDto.getRoleId());
            Role updatedRole = RoleMapper.INSTANCE.roleDtoToRole(roleDto);

            employees.forEach(employee -> {
                employee.setRole(updatedRole);
            });
            employeeRepository.saveAll(employees);

            logger.info("Role {} updated successfully", roleDto.getRoleName());
            return ResponseEntity.ok("Role updated successfully");
        } else {
            logger.error("Role not updated. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }
}
