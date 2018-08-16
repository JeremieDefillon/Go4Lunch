package com.gz.jey.go4lunch.utils

import com.gz.jey.go4lunch.models.Place
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {


    // the request for the google places search api
    //https://maps.googleapis.com/maps/api/place/textsearch/json?type=restaurant&radius=50000&location=46.0056,4.7199&language=fr&key=AIzaSyB2l1D02XIzEFmEecNFAwhe404tHaRS1Sw
    //private val villars: LatLng = LatLng(45.9953225, 5.029484499999967)
    //private val privas: LatLng = LatLng(44.735269, 4.599038999999948)
    @GET("maps/api/place/textsearch/json?")
    fun getRestaurants(
            @Query("type") type: String,
            @Query("location") location: String,
            @Query("radius") radius: String,
            @Query("language") language: String,
            @Query("key") key: String
    ) : Observable<Place>
}