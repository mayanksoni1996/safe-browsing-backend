package tech.mayanksoni.safebrowsing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tech.mayanksoni.safebrowsing.clients.TrancoHttpClient;

@Configuration
public class SpringRestClientsConfiguration {
    private static final String TRANCO_BASE_URL = "https://tranco-list.eu";

    @Bean
    public TrancoHttpClient trancoHttpClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(TRANCO_BASE_URL).build();
        HttpServiceProxyFactory serviceProxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return serviceProxyFactory.createClient(TrancoHttpClient.class);
    }
}
