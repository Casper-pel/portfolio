package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "employee")
public class Employee{

    @Id
    private ObjectId id;
    private Integer employeeId;
    private String firstName;
    private String lastName;
    private String password;
    private Role role;

    public Employee() {}

    public Employee(Integer employeeId, String firstName, String lastName, String password, Role role){
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
    }

    public ObjectId getId(){return id;}

    public void setId(ObjectId id){this.id = id;}

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
        if (role != null) {
            return role.getRolePermissions();
        }
        return new ArrayList<>();
    }

}
