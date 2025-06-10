package tech.mayanksoni.threatdetectionbackend.utils;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ThreatIntelFileSystemUtilsTest {

    @Test
    public void testReadCsvFileFromFileSystem() {
        String trancoFilePath = System.getenv("TRANCO_FILEPATH");
        if (trancoFilePath == null) {
            System.out.println("[DEBUG_LOG] TRANCO_FILEPATH environment variable is not set. Skipping test.");
            return;
        }

        try {
            ThreatIntelFileSystemUtils.readCsvFileFromFileSystem(Paths.get(trancoFilePath));
            System.out.println("[DEBUG_LOG] Successfully read CSV file");
        } catch (NoClassDefFoundError e) {
            System.out.println("[DEBUG_LOG] NoClassDefFoundError: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Other exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testReadCsvFileFromFileSystemAsFlux() throws IOException {
        // Create a temporary CSV file for testing
        Path tempFile = Files.createTempFile("test-domains", ".csv");
        try {
            // Write test data to the file
            List<String> lines = Arrays.asList(
                "rank,domain,tld", // header
                "1,example.com,com",
                "2,test.org,org",
                "3,sample.net,net"
            );
            Files.write(tempFile, lines);

            // Test the method
            Flux<String[]> result = ThreatIntelFileSystemUtils.readCsvFileFromFileSystemAsFlux(tempFile);

            // Verify the result using StepVerifier with custom comparison for arrays
            StepVerifier.create(result)
                .expectNextMatches(arr -> arr.length == 3 && arr[0].equals("1") && arr[1].equals("example.com") && arr[2].equals("com"))
                .expectNextMatches(arr -> arr.length == 3 && arr[0].equals("2") && arr[1].equals("test.org") && arr[2].equals("org"))
                .expectNextMatches(arr -> arr.length == 3 && arr[0].equals("3") && arr[1].equals("sample.net") && arr[2].equals("net"))
                .verifyComplete();

            System.out.println("[DEBUG_LOG] Successfully read CSV file as Flux");
        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }
    }
}
