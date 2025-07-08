package tech.mayanksoni.safebrowsing.cucumber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.mayanksoni.safebrowsing.SafeBrowsingApplication;
import tech.mayanksoni.safebrowsing.TestcontainersConfiguration;

@SpringBootApplication
@Import({TestcontainersConfiguration.class})
public class CucumberTestApplication {

    public static void main(String[] args) {
        SpringApplication.from(SafeBrowsingApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}