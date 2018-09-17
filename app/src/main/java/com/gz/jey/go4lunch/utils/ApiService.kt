package com.gz.jey.go4lunch.utils

import com.gz.jey.go4lunch.models.Details
import com.gz.jey.go4lunch.models.Place
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    /**
     * REQUEST TO GET RESTAURANT AROUND LOCATION
     * @return Observable<Place>
     */
    @GET("maps/api/place/textsearch/json?")
    fun getRestaurants(
        @Query("type") type: String,
        @Query("location") location: String,
        @Query("radius") radius: String,
        @Query("rankBy") distance: String,
        @Query("language") language: String,
        @Query("key") key: String
    ) : Observable<Place>

    /**
     * REQUEST TO GET RESTAURANT DETAILS FROM A PLACE ID
     * @return Observable<Details>
     */
    @GET("maps/api/place/details/json?")
    fun getDetails(
            @Query("placeid") placeid: String,
            @Query("fields") fields: String,
            @Query("language") language: String,
            @Query("key") key: String
    ) : Observable<Details>

}