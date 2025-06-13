package tech.mayanksoni.threatdetectionbackend.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.mayanksoni.threatdetectionbackend.annotations.CreationTimestamp;

import java.time.Instant;

import java.time.Instant;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrustedDomainDocument {
    @Id
    private String id;
    private String domainName;
    private long domainRank;
    @Indexed
    private String tld;
    private char firstLetter;
    private String soundexCode;
    private String metaphoneCode;
    private String doubleMetaphoneCode;
    private int length;
    @CreationTimestamp
    private Instant createdAt;
}
