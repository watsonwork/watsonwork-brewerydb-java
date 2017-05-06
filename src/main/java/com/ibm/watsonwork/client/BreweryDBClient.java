package com.ibm.watsonwork.client;

import com.ibm.watsonwork.model.brewerydb.Brewery;
import com.ibm.watsonwork.model.brewerydb.BreweryResponseGeo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface BreweryDBClient {

    String GEO_SEARCH_PATH = "/v2/search/geo/point";
    String HTTP_ACCEPT_JSON="HTTP-ACCEPT: application/json";

    @Headers({HTTP_ACCEPT_JSON})
    @GET(GEO_SEARCH_PATH)
    Call<BreweryResponseGeo> geoSearch(@Query("key") String key, @Query("lat") String lat, @Query("lng") String lng, @Query("radius") String radius);

    @Headers({HTTP_ACCEPT_JSON})
    @GET("/v2/brewery/{breweryId}")
    Call<Brewery> getBrewery(@Path("breweryId") String spaceId,@Query("key") String key);


}
