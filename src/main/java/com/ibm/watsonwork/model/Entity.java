package com.ibm.watsonwork.model;

import lombok.Data;

@Data
public class Entity {

    private String text;

    private String type;

    private String source;

    private String count;

    private String relevance;

    private Object location;

}
