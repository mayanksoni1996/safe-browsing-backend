package tech.mayanksoni.safebrowsing.cucumber;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tech.mayanksoni.safebrowsing.clients.TrancoHttpClient;
import tech.mayanksoni.safebrowsing.models.TrancoDailyFileMetadata;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Primary
public class MockTrancoHttpClient implements TrancoHttpClient {

    private static final String TEST_CSV_CONTENT = "1,example.com\n2,example.org\n3,example.net";
    private static final String TEST_LIST_ID = "test-list-id";

    @Override
    public TrancoDailyFileMetadata getTrancoFileMetadataByDate(String formattedDate) {
        TrancoDailyFileMetadata metadata = new TrancoDailyFileMetadata();
        metadata.setListId(TEST_LIST_ID);
        metadata.setAvailable(true);
        metadata.setFailed(false);
        metadata.setCreatedOn(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        metadata.setDownload("/download/" + TEST_LIST_ID + "/full");
        return metadata;
    }

    @Override
    public Resource downloadTrancoFullListById(String listId) {
        return new ByteArrayResource(TEST_CSV_CONTENT.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Resource downloadTrancoTop1MList(String listId) {
        return new ByteArrayResource(TEST_CSV_CONTENT.getBytes(StandardCharsets.UTF_8));
    }
}
