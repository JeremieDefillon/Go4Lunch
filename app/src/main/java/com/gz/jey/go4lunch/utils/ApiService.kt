package com.gz.jey.go4lunch.utils

import com.gz.jey.go4lunch.models.Details
import com.gz.jey.go4lunch.models.Place
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {


    // the request for the google places search api
    //https://maps.googleapis.com/maps/api/place/textsearch/json?type=restaurant&radius=50000&location=46.0056,4.7199&language=fr&key=KEY
    @GET("maps/api/place/textsearch/json?")
    fun getRestaurants(
        @Query("type") type: String,
        @Query("location") location: String,
        @Query("radius") radius: String,
        @Query("rankBy") distance: String,
        @Query("language") language: String,
        @Query("key") key: String
    ) : Observable<Place>

    @GET("maps/api/place/details/json?")
    fun getDetails(
            @Query("placeid") placeid: String,
            @Query("fields") fields: String,
            @Query("language") language: String,
            @Query("key") key: String
    ) : Observable<Details>

}