package com.ibm.watsonwork.model.brewerydb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Brewery {

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private BreweryData data;

    @JsonProperty("status")
    private String status;

}