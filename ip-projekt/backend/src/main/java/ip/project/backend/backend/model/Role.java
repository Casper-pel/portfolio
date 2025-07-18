package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "role")
public class Role {
    @Id
    private ObjectId _id;
    private Integer roleId;
    private String roleName;
    private String description;
    private List<String> rolePermissions;

    public Role() {
    }

    public Role(Integer roleId, String roleName, String description, List<String> rolePermissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.rolePermissions = rolePermissions;
    }

    public ObjectId get_id() {
        return _id;
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
        return rolePermissions != null ? rolePermissions : new ArrayList<>();
    }
    public void setRolePermissions(List<String> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}