package ip.project.backend.backend.mapper;

import ip.project.backend.backend.model.Stock;
import ip.project.backend.backend.modeldto.StockDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StockMapper {
    StockMapper INSTANCE = Mappers.getMapper(StockMapper.class);

//    @Mapping(target = "_id", ignore = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "repurchased", source = "repurchased")
    @Mapping(target = "shouldBeRepurchased", source = "shouldBeRepurchased")
    StockDto stockToDto(Stock stock);


    @Mapping(target = "_id", ignore = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "repurchased", source = "repurchased")
    @Mapping(target = "shouldBeRepurchased", source = "shouldBeRepurchased")
    Stock stockDtoToStock(StockDto stockDto);
}
