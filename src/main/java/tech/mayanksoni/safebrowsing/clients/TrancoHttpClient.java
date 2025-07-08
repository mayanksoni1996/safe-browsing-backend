package tech.mayanksoni.safebrowsing.clients;

import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import tech.mayanksoni.safebrowsing.models.TrancoDailyFileMetadata;

@HttpExchange
public interface TrancoHttpClient {
    @GetExchange("/api/lists/date/{formatted-date}")
    TrancoDailyFileMetadata getTrancoFileMetadataByDate(@PathVariable("formatted-date") String formattedDate);

    @GetExchange("/download/{listId}/full")
    Resource downloadTrancoFullListById(@PathVariable("listId") String listId);

    @GetExchange("/download/{listId}/1000000")
    Resource downloadTrancoTop1MList(@PathVariable("listId") String listId);
}
