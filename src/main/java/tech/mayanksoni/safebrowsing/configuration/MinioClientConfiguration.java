package tech.mayanksoni.safebrowsing.configuration;

import io.minio.MinioClient;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "minio")
public class MinioClientConfiguration {
    private String endpoint;
    private String accessKey;
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        log.info("Initializing Minio client using Access Key ID :{}", accessKey);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
