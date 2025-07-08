package tech.mayanksoni.safebrowsing.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrancoDailyFileMetadata {

    @JsonProperty("list_id")
    private String listId;
    @JsonProperty("available")
    private Boolean available;
    @JsonProperty("failed")
    private Boolean failed;
    @JsonProperty("download")
    private String download;
    @JsonProperty("created_on")
    private String createdOn;

}
