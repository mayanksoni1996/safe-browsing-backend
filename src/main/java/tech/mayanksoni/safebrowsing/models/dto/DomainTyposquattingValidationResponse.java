package tech.mayanksoni.safebrowsing.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Response object for domain typosquatting validation results")
public class DomainTyposquattingValidationResponse {
    @Schema(description = "The original domain that was checked", example = "g00gle.com")
    private String originalDomain;

    @Schema(description = "Indicates whether typosquatting was detected for the domain", example = "true")
    private boolean typosquattingDetected;

    @Schema(description = "The closest matching legitimate domain found", example = "google.com")
    private String closestMatchingDomain;
}
