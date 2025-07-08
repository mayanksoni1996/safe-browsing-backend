package tech.mayanksoni.safebrowsing;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    MinIOContainer minioContainer() {
        return new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
                .withUserName("minioadmin")
                .withPassword("minioadmin");
    }

    @Bean
    @Primary
    String minioEndpoint(MinIOContainer minioContainer) {
        return minioContainer.getS3URL();
    }

    @Bean
    @Primary
    String minioAccessKey() {
        return "minioadmin";
    }

    @Bean
    @Primary
    String minioSecretKey() {
        return "minioadmin";
    }
}
