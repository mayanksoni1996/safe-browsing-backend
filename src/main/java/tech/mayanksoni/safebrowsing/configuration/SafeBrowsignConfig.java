package tech.mayanksoni.safebrowsing.configuration;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "safe-browsing")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SafeBrowsignConfig {
    private String minioBucketName;
    @Builder.Default
    private int dataLoadBatch = 100000;
    @Builder.Default
    private int editDistanceThreshold = 2;
    @Builder.Default
    private boolean phoneticMatchingEnabled = false;
}
