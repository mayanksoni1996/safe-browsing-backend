package tech.mayanksoni.safebrowsing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tech.mayanksoni.safebrowsing.documents.TrancoFile;
import tech.mayanksoni.safebrowsing.models.TrancoFileEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrancoFileInformationMapper {

    @Mapping(source = "id", target = "databaseRecordId")
    TrancoFileEntity toTrancoFileEntity(TrancoFile sourceTrancoFile);
}
