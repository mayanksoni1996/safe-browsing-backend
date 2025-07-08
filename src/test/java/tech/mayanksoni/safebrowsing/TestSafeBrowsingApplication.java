package tech.mayanksoni.safebrowsing;

import org.springframework.boot.SpringApplication;

public class TestSafeBrowsingApplication {

    public static void main(String[] args) {
        SpringApplication.from(SafeBrowsingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
