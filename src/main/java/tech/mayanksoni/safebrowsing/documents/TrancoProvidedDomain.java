package tech.mayanksoni.safebrowsing.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrancoProvidedDomain {
    @Id
    private String id;
    private long domainRank;
    private String domain;
    private String listId;
    private String registrySuffix;
    private char domainFirstCharacter;
    private String ownerDomainSoundexCode;
    private String ownerDomainMetaphoneCode;
    private String ownerDomainDoubleMetaphoneCode;
    private int domainLength;
}
