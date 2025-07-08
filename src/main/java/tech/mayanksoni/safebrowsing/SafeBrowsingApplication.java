package tech.mayanksoni.safebrowsing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tech.mayanksoni.safebrowsing.configuration.MinioClientConfiguration;
import tech.mayanksoni.safebrowsing.configuration.SafeBrowsignConfig;

@SpringBootApplication
@EnableConfigurationProperties({MinioClientConfiguration.class, SafeBrowsignConfig.class})
public class SafeBrowsingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeBrowsingApplication.class, args);
    }

}
