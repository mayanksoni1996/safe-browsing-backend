package tech.mayanksoni.threatdetectionbackend.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StateDocument {
    @Id
    private String id;
    @Indexed
    private String domainName;
    @Indexed
    private String ipAddress;
    private Instant creationTimestamp;
    private Instant stateExpiresAt;
    private boolean accessOverrideControlAvailable;
    private boolean accessAllowed;
}
