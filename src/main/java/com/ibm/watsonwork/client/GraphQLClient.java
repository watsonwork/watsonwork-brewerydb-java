package com.ibm.watsonwork.client;

import com.ibm.watsonwork.model.GraphQLResponse;
import com.ibm.watsonwork.model.GraphQLQuery;
import retrofit2.Call;
import retrofit2.http.*;


public interface GraphQLClient {

    String CONTENT_JSON_TYPE = "Content-Type: application/json";
    String GRAPHQL_VIEW = "x-graphql-view: ACTIONS,PUBLIC";
    String GRAPHQL_PATH = "/graphql";

    @Headers({ CONTENT_JSON_TYPE, GRAPHQL_VIEW })
    @POST(GRAPHQL_PATH)
    Call<GraphQLResponse> createMessage(@Header("Authorization") String authToken, @Body GraphQLQuery query);

    @Headers({ CONTENT_JSON_TYPE, GRAPHQL_VIEW })
    @POST(GRAPHQL_PATH)
    Call<GraphQLResponse> createTargetedMessage(@Header("Authorization") String authToken, @Body GraphQLQuery query);

}
