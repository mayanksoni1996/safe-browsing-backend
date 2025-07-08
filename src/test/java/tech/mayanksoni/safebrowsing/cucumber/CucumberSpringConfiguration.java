package tech.mayanksoni.safebrowsing.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberTestApplication.class)
public class CucumberSpringConfiguration {
    // This class is used to configure Cucumber to use Spring's test context
    // It uses CucumberTestApplication which imports TestcontainersConfiguration

    @Bean
    @Scope("cucumber-glue")
    public TrancoListStepDefinitions trancoListStepDefinitions() {
        return new TrancoListStepDefinitions();
    }
}
