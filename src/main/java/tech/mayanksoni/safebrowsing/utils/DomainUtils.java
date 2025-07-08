package tech.mayanksoni.safebrowsing.utils;

import com.google.common.net.InternetDomainName;
import lombok.extern.slf4j.Slf4j;
import tech.mayanksoni.safebrowsing.models.DomainFeatures;
import tech.mayanksoni.safebrowsing.models.PhoneticModel;
import tech.mayanksoni.safebrowsing.models.TrancoCSVFileRecord;

import java.util.Objects;

@Slf4j
public class DomainUtils {
    public static DomainFeatures extractDomainFeatures(String domainName, long domainRankFromTranco) {
        try {
            InternetDomainName idn = InternetDomainName.from(domainName.toLowerCase().trim());
            String registrySuffix = Objects.requireNonNull(idn.registrySuffix().toString());
            String ownerDomain = extractPrivateDomainFromTopDomainUnderRegistrySuffix(idn.topDomainUnderRegistrySuffix());
            if (idn.isUnderRegistrySuffix()) {
                return DomainFeatures.builder()
                        .domainLength(idn.topDomainUnderRegistrySuffix().toString().length())
                        .originalDomain(domainName)
                        .phoneticModel(
                                new PhoneticModel(PhoneticUtils.encodeSoundex(ownerDomain),
                                        PhoneticUtils.encodeMetaphone(ownerDomain),
                                        PhoneticUtils.encodeDoubleMetaphone(ownerDomain))
                        )
                        .domainRankFromTranco(domainRankFromTranco)
                        .idn(idn)
                        .privateDomain(ownerDomain)
                        .registrySuffix(registrySuffix)
                        .build();
            }
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            log.error("Invalid domain name {}", domainName);
            return null;
        }
        return null;
    }

    public static DomainFeatures extractDomainFeatures(TrancoCSVFileRecord csvFileRecord) {
        return extractDomainFeatures(csvFileRecord.domain(), csvFileRecord.rank());
    }

    private static String extractPrivateDomainFromTopDomainUnderRegistrySuffix(InternetDomainName topDomainUnderRegistrySuffix) {
        String registrySuffix = topDomainUnderRegistrySuffix.registrySuffix().toString();
        int lastIndex = topDomainUnderRegistrySuffix.toString().lastIndexOf("." + registrySuffix);
        if (lastIndex == -1) {
            return null;
        }
        return topDomainUnderRegistrySuffix.toString().substring(0, lastIndex);
    }
}
