package com.ibm.watsonwork.client;

import com.ibm.watsonwork.model.BreweryResponse;
import com.ibm.watsonwork.model.Brewery;
import retrofit2.Call;
import retrofit2.http.*;


public interface BreweryDBClient {

    String SEARCH_PATH = "/v2/search";
    String BREWERY_PATH = "/v2/brewery/";
    String HTTP_ACCEPT_JSON="HTTP-ACCEPT: application/json";

    @Headers({HTTP_ACCEPT_JSON})
    @GET(SEARCH_PATH)
    Call<BreweryResponse> search(@Query("key") String key, @Query("q") String query);

    @Headers({HTTP_ACCEPT_JSON})
    @GET("/v2/brewery/{breweryId}")
    Call<Brewery> getBrewery(@Path("breweryId") String spaceId,@Query("key") String key);


}
