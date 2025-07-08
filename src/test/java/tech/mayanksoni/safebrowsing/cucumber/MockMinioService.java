package tech.mayanksoni.safebrowsing.cucumber;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tech.mayanksoni.safebrowsing.services.MinioService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class MockMinioService extends MinioService {

    private static final String TEST_CSV_CONTENT = "1,example.com\n2,example.org\n3,example.net";
    private final Map<String, String> fileContents = new HashMap<>();

    public MockMinioService() {
        super(null, null); // Pass null for required dependencies
    }

    @Override
    public void uploadFile(String fileName, java.io.InputStream inputStream) {
        try {
            // Read the input stream and store it in the map
            byte[] bytes = inputStream.readAllBytes();
            fileContents.put(fileName, new String(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Error reading input stream", e);
        }
    }

    @Override
    public BufferedReader downloadFile(String fileName) {
        // Return the file content from the map, or a default test content if not found
        String content = fileContents.getOrDefault(fileName, TEST_CSV_CONTENT);
        return new BufferedReader(new StringReader(content));
    }

    @Override
    public boolean isFilePresent(String fileName) {
        return fileContents.containsKey(fileName);
    }

    // Override initMinioService to do nothing
    @Override
    public void initMinioService() {
        // Do nothing
    }
}