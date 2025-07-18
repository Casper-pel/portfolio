package ip.project.backend.backend.mapper;

import ip.project.backend.backend.model.UrlaubsAntrag;
import ip.project.backend.backend.modeldto.UrlaubsAntragDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UrlaubsAntragMapper {
    UrlaubsAntragMapper INSTANCE = Mappers.getMapper(UrlaubsAntragMapper.class);

    @Mapping(target = "antragsId", source = "antragsId")
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "startDatum", source = "startDatum")
    @Mapping(target = "endDatum", source = "endDatum")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "grund", source = "grund")
    @Mapping(target = "reviewDate", source = "reviewDate")
    @Mapping(target = "reviewerId", source = "reviewerId")
    @Mapping(target = "comment", source = "comment")
    UrlaubsAntragDto urlaubsAntragToUrlaubsAntragDto(UrlaubsAntrag urlaubsAntrag);

    @Mapping(target = "antragsId", source = "antragsId")
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "startDatum", source = "startDatum")
    @Mapping(target = "endDatum", source = "endDatum")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "grund", source = "grund")
    @Mapping(target = "reviewDate", source = "reviewDate")
    @Mapping(target = "reviewerId", source = "reviewerId")
    @Mapping(target = "comment", source = "comment")
    UrlaubsAntrag urlaubsAntragDtoToUrlaubsAntrag(UrlaubsAntragDto urlaubsAntragDto);
}
