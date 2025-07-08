package tech.mayanksoni.safebrowsing.models;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "filterTLD",
        "providers",
        "combinationMethod",
        "isDailyList",
        "listPrefix",
        "endDate",
        "startDate",
        "filterPLD"
})
public class Configuration {

    @JsonProperty("filterTLD")
    private String filterTLD;
    @JsonProperty("providers")
    private List<String> providers;
    @JsonProperty("combinationMethod")
    private String combinationMethod;
    @JsonProperty("isDailyList")
    private Boolean isDailyList;
    @JsonProperty("listPrefix")
    private String listPrefix;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("filterPLD")
    private String filterPLD;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("filterTLD")
    public String getFilterTLD() {
        return filterTLD;
    }

    @JsonProperty("filterTLD")
    public void setFilterTLD(String filterTLD) {
        this.filterTLD = filterTLD;
    }

    @JsonProperty("providers")
    public List<String> getProviders() {
        return providers;
    }

    @JsonProperty("providers")
    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

    @JsonProperty("combinationMethod")
    public String getCombinationMethod() {
        return combinationMethod;
    }

    @JsonProperty("combinationMethod")
    public void setCombinationMethod(String combinationMethod) {
        this.combinationMethod = combinationMethod;
    }

    @JsonProperty("isDailyList")
    public Boolean getIsDailyList() {
        return isDailyList;
    }

    @JsonProperty("isDailyList")
    public void setIsDailyList(Boolean isDailyList) {
        this.isDailyList = isDailyList;
    }

    @JsonProperty("listPrefix")
    public String getListPrefix() {
        return listPrefix;
    }

    @JsonProperty("listPrefix")
    public void setListPrefix(String listPrefix) {
        this.listPrefix = listPrefix;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("filterPLD")
    public String getFilterPLD() {
        return filterPLD;
    }

    @JsonProperty("filterPLD")
    public void setFilterPLD(String filterPLD) {
        this.filterPLD = filterPLD;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
