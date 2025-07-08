package tech.mayanksoni.safebrowsing.controllers.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mayanksoni.safebrowsing.models.TyposquattingValidationResponse;
import tech.mayanksoni.safebrowsing.models.dto.DomainTyposquattingValidationRequest;
import tech.mayanksoni.safebrowsing.services.TyposquattingDetectionService;

@RestController
@RequestMapping("/v1/phishproof")
@RequiredArgsConstructor
@Tag(name = "PhishProof", description = "API for phishing and typosquatting detection")
public class PhishProofV1Controller {
    private final TyposquattingDetectionService typosquattingDetectionService;

    @Operation(
            summary = "Check domain for typosquatting",
            description = "Analyzes a domain to detect potential typosquatting attacks by comparing it with known trusted domains"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Domain successfully analyzed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TyposquattingValidationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/check-typosquatting")
    public ResponseEntity<TyposquattingValidationResponse> checkDomainForTyposquatting(
            @Parameter(description = "Domain validation request containing the domain to check", required = true)
            @RequestBody DomainTyposquattingValidationRequest validationRequest) {
        return ResponseEntity.ok(this.typosquattingDetectionService.checkDomainForTyposquatting(validationRequest));
    }
}
