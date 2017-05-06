package com.ibm.watsonwork.model.brewerydb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BreweryDataGeoSearch {

    @JsonProperty("brewery")
    private BreweryDataGeo brewery;

}