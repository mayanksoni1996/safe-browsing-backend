package tech.mayanksoni.safebrowsing.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrancoFile {
    @Id
    private String id;
    private String listId;
    private Instant downloadedOn;
    private boolean processed;
    private boolean purged;
    private boolean active;
    private long recordCount;
    private long sizeInBytes;
}
