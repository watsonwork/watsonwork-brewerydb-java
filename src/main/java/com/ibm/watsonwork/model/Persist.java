package com.ibm.watsonwork.model;

import java.util.ArrayList;

import lombok.Data;
import org.springframework.stereotype.Component;


@Component
@Data
public class Persist {

    private ArrayList<PersistObject> persistObjects;
}
