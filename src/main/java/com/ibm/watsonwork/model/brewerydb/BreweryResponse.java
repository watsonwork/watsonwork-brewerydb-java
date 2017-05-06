package com.ibm.watsonwork.model.brewerydb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BreweryResponse {

    @JsonProperty("currentPage")
    private String currentPage;

    @JsonProperty("numberOfPages")
    private String numberOfPages;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<BreweryData> data;

    @JsonProperty("status")
    private String status;

}