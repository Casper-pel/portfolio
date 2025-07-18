package ip.project.backend.backend.mapper;

import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.modeldto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    EmployeeDto employeeToEmployeeDto(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    Employee employeeDtoToEmployee(EmployeeDto employeeDto);
}
