package com.ibm.watsonwork.model.brewerydb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BreweryData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nameShortDisplay")
    private String nameShortDisplay;

    @JsonProperty("isOrganic")
    private String isOrganic;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusDisplay")
    private String statusDisplay;

    @JsonProperty("createDate")
    private String createDate;

    @JsonProperty("updateDate")
    private String updateDate;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("website")
    private String website;

    @JsonProperty("established")
    private String established;

}