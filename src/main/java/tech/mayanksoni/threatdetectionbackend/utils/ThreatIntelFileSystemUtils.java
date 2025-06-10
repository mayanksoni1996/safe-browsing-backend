package tech.mayanksoni.threatdetectionbackend.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ThreatIntelFileSystemUtils {
    private static final String ERROR_TRANCO_FILEPATH_NOT_SET = "TRANCO_FILEPATH environment variable is not set";

    @Deprecated
    public static List<String[]> readCsvFileFromFileSystem(Path csvFilePath) {
        if (csvFilePath == null) {
            log.warn(ERROR_TRANCO_FILEPATH_NOT_SET);
            return new ArrayList<>();
        }

        Resource csvResource = new FileSystemResource(csvFilePath.toFile());
        List<String[]> csvRecords = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvResource.getFile()))) {
            reader.readNext(); // Skip header line
            String[] line;
            while ((line = reader.readNext()) != null) {
                csvRecords.add(line);
            }
            return csvRecords;
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
    public static Flux<String[]> readCsvFileFromFileSystemAsFlux(Path csvFilePath) throws IOException {
        if (csvFilePath == null) {
            log.warn(ERROR_TRANCO_FILEPATH_NOT_SET);
            return Flux.empty();
        }
        Resource csvResource = new FileSystemResource(csvFilePath.toFile());

        return Flux.using(
            // Resource factory: creates the resource
            () -> new BufferedReader(new FileReader(csvResource.getFile())),
            // Stream factory: creates a Flux from the resource
            br -> Flux.fromStream(br.lines()
                    .skip(1)
                    .map(line -> line.split(","))),
            // Resource cleanup: closes the resource when the Flux completes
            br -> {
                try {
                    br.close();
                    log.debug("BufferedReader closed successfully");
                } catch (IOException e) {
                    log.error("Error closing BufferedReader: {}", e.getMessage());
                }
            }
        );
    }
}
