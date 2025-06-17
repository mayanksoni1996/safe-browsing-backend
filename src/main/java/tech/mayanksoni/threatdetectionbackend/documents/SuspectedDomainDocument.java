package tech.mayanksoni.threatdetectionbackend.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SuspectedDomainDocument {
    @Id
    private String id;
    private String domainName;
    private String reason;
    private String source;
    @Indexed
    private String tld;

}