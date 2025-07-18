package ip.project.backend.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.modeldto.RoleDto;

@Mapper
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    Role roleDtoToRole(RoleDto roleDto);

    @Mapping(target = "employeeDtos", ignore = true)
    RoleDto roleToRoleDto(Role role);
}
