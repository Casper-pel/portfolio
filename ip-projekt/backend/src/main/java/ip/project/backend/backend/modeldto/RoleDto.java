package ip.project.backend.backend.modeldto;

import java.util.ArrayList;
import java.util.List;

public class RoleDto {
    private Integer roleId;
    private String roleName;
    private String description;
    private List<String> rolePermissions;
    private List<EmployeeDto> employeeDtos; // Feld für Response-Mapping

    public RoleDto() {
        this.employeeDtos = new ArrayList<>();
    }

    public RoleDto(Integer roleId, String roleName, String description, List<String> rolePermissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.rolePermissions = rolePermissions;
        this.employeeDtos = new ArrayList<>();
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<String> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }

    public List<EmployeeDto> getEmployeeDtos() {
        return employeeDtos;
    }

    public void setEmployeeDtos(List<EmployeeDto> employeeDtos) {
        this.employeeDtos = employeeDtos;
    }
}