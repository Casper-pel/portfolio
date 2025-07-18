package ip.project.backend.backend.modeldto;

import ip.project.backend.backend.model.Role;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDto{

    @NotNull(message = "Mitarbeiter Id ist erforderlich")
    private Integer employeeId;
    @NotNull(message = "Vorname ist required")
    private String firstName;
    @NotNull(message = "Nachname ist erforderlich")
    private String lastName;
    @NotNull(message = "Passwort ist erforderlich")
    private String password;
    private Role role;

    public EmployeeDto() {}

    public EmployeeDto(Integer employeeId, String firstName, String lastName, String password, Role role){
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
    }


    public Integer getEmployeeId(){return employeeId;}

    public void setEmployeeId(Integer employeeId){this.employeeId = employeeId;}

    public String getFirstName(){return firstName;}

    public void setFirstName(String firstName){this.firstName = firstName;}

    public String getLastName(){
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole(){return role;}

    public void setRole(Role role){this.role = role;  }

    public List<String> getRolePermissions() {
        if (role != null && role.getRolePermissions() != null) {
            return role.getRolePermissions();
        }
        return new ArrayList<>();
    }

}
