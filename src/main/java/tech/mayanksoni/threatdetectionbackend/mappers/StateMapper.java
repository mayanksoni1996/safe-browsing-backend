package tech.mayanksoni.threatdetectionbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import tech.mayanksoni.threatdetectionbackend.documents.StateDocument;
import tech.mayanksoni.threatdetectionbackend.models.StateModel;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StateMapper {
    @Mapping(source = "id", target = "state")
    StateModel toStateModel(StateDocument stateDocument);
}
