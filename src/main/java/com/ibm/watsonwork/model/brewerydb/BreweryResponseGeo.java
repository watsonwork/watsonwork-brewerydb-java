package com.ibm.watsonwork.model.brewerydb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BreweryResponseGeo {

    @JsonProperty("currentPage")
    private String currentPage;

    @JsonProperty("numberOfPages")
    private String numberOfPages;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("data")
    private List<BreweryDataGeoSearch> data;

    @JsonProperty("status")
    private String status;

}