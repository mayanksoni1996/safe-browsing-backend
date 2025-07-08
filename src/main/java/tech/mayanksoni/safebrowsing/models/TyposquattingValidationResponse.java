package tech.mayanksoni.safebrowsing.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing typosquatting validation results")
public record TyposquattingValidationResponse(
        @Schema(description = "Indicates whether the domain is a potential typosquatting attempt", example = "true")
        boolean isTyposquatting,

        @Schema(description = "The reference domain that matched with the domain under check", example = "google.com")
        String matchedReferenceDomain,

        @Schema(description = "The domain that was checked for typosquatting", example = "g00gle.com")
        String domainUnderCheck,

        @Schema(description = "Indicates whether phonetic matching was used in the validation", example = "true")
        boolean phoneticMatchEnabled
) {
}
