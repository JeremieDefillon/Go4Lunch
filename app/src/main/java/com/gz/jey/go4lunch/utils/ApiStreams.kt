package com.gz.jey.go4lunch.utils

import com.google.android.gms.maps.model.LatLng
import com.gz.jey.go4lunch.BuildConfig
import com.gz.jey.go4lunch.models.Details
import com.gz.jey.go4lunch.models.Place
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiStreams {

    /**
     * RETROFIT BUILDER
     * @return Retrofit
     */
    private val retrofit: Retrofit
        get() {
            return Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }

    /**
     * FETCH RESTAURANTS LIST REQUEST WITH GOOGLE PLACE
     * @param loc LatLng
     * @param lang Int
     * @return Observable<Place>
     */
    fun streamFetchRestaurants(loc: LatLng , lang: Int): Observable<Place> {
        val location = loc.latitude.toString()+","+loc.longitude.toString()
        //location = "45.750000,4.850000"
        val radius = "8000"
        val rankby = "distance"
        val language = if (lang==1) "fr" else "en"
        val type = "restaurant"

        val apiService = this.retrofit.create(ApiService::class.java)

        return apiService.getRestaurants(type, location, radius, rankby, language, BuildConfig.GOOGLE_MAPS_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS)
    }

    /**
     * FETCH RESTAURANT DETAILS REQUEST WITH GOOGLE PLACE
     * @param id String
     * @param lang Int
     * @return Observable<Details>
     */
    fun streamFetchDetails(id:String, lang: Int): Observable<Details> {
        val fields = "place_id,name,vicinity,photo,opening_hours,rating,international_phone_number,formatted_phone_number,website,types"
        val language = if (lang==1) "fr" else "en"

        val apiService = this.retrofit.create(ApiService::class.java)

        return apiService.getDetails(id , fields, language, BuildConfig.GOOGLE_MAPS_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS)
    }



}