package tech.mayanksoni.safebrowsing.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI documentation for the Safe Browsing API.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI safeBrowsingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Safe Browsing API")
                        .description("API for detecting and preventing phishing and typosquatting attacks")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Mayank Soni")
                                .url("https://mayanksoni.tech")
                                .email("contact@mayanksoni.tech"))
                        .license(new License()
                                .name("API License")
                                .url("https://mayanksoni.tech/license")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server URL")
                ));
    }
}