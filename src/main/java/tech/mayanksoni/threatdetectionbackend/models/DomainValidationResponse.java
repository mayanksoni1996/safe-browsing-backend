package tech.mayanksoni.threatdetectionbackend.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Response for domain validation")
@Builder
public record DomainValidationResponse(
        @Schema(description = "The state of the domain")
        StateModel stateModel,
        @Schema(description = "The Result of Domain Typosquatting Validation")
        DomainTyposquattingValidationResults typosquattingValidationResults
) {
}
