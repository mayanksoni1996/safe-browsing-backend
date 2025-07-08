package tech.mayanksoni.safebrowsing.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for domain typosquatting validation")
public class DomainTyposquattingValidationRequest {
    @Schema(description = "The domain to check for typosquatting", example = "example.com", required = true)
    private String domain;

    @Schema(description = "The state of the domain (e.g., active, inactive)", example = "active")
    private String state;

    @Schema(description = "The resolved IP address of the domain", example = "192.168.1.1")
    private String resolvedIpAddress;
}
