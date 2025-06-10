package tech.mayanksoni.threatdetectionbackend.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mayanksoni.threatdetectionbackend.dm.TrustedDomainDataManager;

@RestController
@RequestMapping("/api/v1/trusted-domains/manage")
@RequiredArgsConstructor
@Tag(name = "Trusted Domains Management", description = "API for managing trusted domains")
public class TrustedDomainsManagementController {
    private final TrustedDomainDataManager trustedDomainDataManager;

    @DeleteMapping("/truncate")
    @Operation(summary = "Truncate trusted domains", description = "Removes all trusted domains from the database")
    private void truncateTrustedDomains() {
        this.trustedDomainDataManager.truncateTrustedDomains().subscribe();
    }
}
