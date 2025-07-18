package ip.project.backend.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.modeldto.EmployeeDto;
import ip.project.backend.backend.modeldto.RoleDto;
import ip.project.backend.backend.service.JwtService;
import ip.project.backend.backend.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RoleController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RoleDto exampleDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        // RoleDto ohne Employee-IDs erstellen
        exampleDto = new RoleDto(
                1,
                "Admin",
                "Administrator",
                List.of("READ", "WRITE")
        );

        EmployeeDto emp1 = new EmployeeDto();
        emp1.setEmployeeId(1);
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        emp1.setPassword(null); // Passwort nicht im DTO

        EmployeeDto emp2 = new EmployeeDto();
        emp2.setEmployeeId(2);
        emp2.setFirstName("Jane");
        emp2.setLastName("Smith");
        emp2.setPassword(null); // Passwort nicht im DTO

        // EmployeeDtos für die Response setzen
        List<EmployeeDto> employeeDtos = new ArrayList<>();
        employeeDtos.add(emp1);
        employeeDtos.add(emp2);
        exampleDto.setEmployeeDtos(employeeDtos);
    }

    @Test
    void getRole_NotFound() throws Exception {
        when(roleService.getRoleByRoleId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/role/get/{id}", 1))
                .andExpect(status().isNoContent());

        verify(roleService).getRoleByRoleId(1);
    }

    @Test
    void getRole_Found() throws Exception {
        when(roleService.getRoleByRoleId(1)).thenReturn(Optional.of(exampleDto));

        mockMvc.perform(get("/api/role/get/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(1))
                .andExpect(jsonPath("$.roleName").value("Admin"))
                .andExpect(jsonPath("$.description").value("Administrator"))
                .andExpect(jsonPath("$.rolePermissions").isArray())
                .andExpect(jsonPath("$.employeeDtos").isArray())
                .andExpect(jsonPath("$.employeeDtos.length()").value(2));

        verify(roleService).getRoleByRoleId(1);
    }

    @Test
    void getAllRoles_Empty() throws Exception {
        when(roleService.getAllRoles()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/role/all"))
                .andExpect(status().isNoContent());

        verify(roleService).getAllRoles();
    }

    @Test
    void getAllRoles_Found() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(exampleDto));

        mockMvc.perform(get("/api/role/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].roleId").value(1));

        verify(roleService).getAllRoles();
    }

    @Test
    void addRole_Success() throws Exception {
        // RoleDto für Request ohne employeeDtos
        RoleDto requestDto = new RoleDto(1, "Admin", "Administrator", List.of("READ", "WRITE"));

        when(roleService.addRole(any(RoleDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/role/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Role added successfully"));

        verify(roleService).addRole(any(RoleDto.class));
    }

    @Test
    void addRole_AlreadyExists() throws Exception {
        RoleDto requestDto = new RoleDto(1, "Admin", "Administrator", List.of("READ", "WRITE"));

        when(roleService.addRole(any(RoleDto.class))).thenReturn(Optional.of("Role already exists"));

        mockMvc.perform(post("/api/role/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role already exists"));

        verify(roleService).addRole(any(RoleDto.class));
    }


    @Test
    void updateRole_NotFound() throws Exception {
        RoleDto requestDto = new RoleDto(1, "Admin", "Administrator", List.of("READ", "WRITE"));

        when(roleService.updateRole(any(RoleDto.class))).thenReturn(Optional.of("Role not found"));

        mockMvc.perform(put("/api/role/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role not found"));

        verify(roleService).updateRole(any(RoleDto.class));
    }



    @Test
    void deleteRole_NotFound() throws Exception {
        when(roleService.deleteRole("1")).thenReturn(Optional.of("Role not found"));

        mockMvc.perform(delete("/api/role/delete/{id}", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role not found"));

        verify(roleService).deleteRole("1");
    }
}
