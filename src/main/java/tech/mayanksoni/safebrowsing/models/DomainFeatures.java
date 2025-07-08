package tech.mayanksoni.safebrowsing.models;

import com.google.common.net.InternetDomainName;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DomainFeatures {
    String registrySuffix;
    String privateDomain;
    String originalDomain;
    long domainRankFromTranco;
    PhoneticModel phoneticModel;
    int domainLength;
    InternetDomainName idn;
}
