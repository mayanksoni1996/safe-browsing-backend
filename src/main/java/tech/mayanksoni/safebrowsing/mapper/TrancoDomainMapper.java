package tech.mayanksoni.safebrowsing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tech.mayanksoni.safebrowsing.documents.TrancoProvidedDomain;
import tech.mayanksoni.safebrowsing.models.TrancoProvidedDomainEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrancoDomainMapper {
    @Mapping(source = "id", target = "databaseIdentifier")
    TrancoProvidedDomainEntity toTrancoProvidedDomainEntity(TrancoProvidedDomain sourceTrancoProvidedDomain);
}
