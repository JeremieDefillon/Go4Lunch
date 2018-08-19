package com.gz.jey.go4lunch.utils

import com.google.android.gms.maps.model.LatLng
import com.gz.jey.go4lunch.models.Place
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiStreams {

    // creating and configuring the retrofit
    // building retrofit
    private val retrofit: Retrofit
        get() {
            val retrofit : Retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()

            return retrofit
        }

    // method to call and pass the request via an observable for the GooglePlace API
    fun streamFetchRestaurants(key: String, loc: LatLng , lang: Int): Observable<Place> {
        var location = loc.latitude.toString()+","+loc.longitude.toString()
        //location = "45.750000,4.850000"
        val radius = "10000"
        val rankby = "distance"
        val language = if (lang==1) "fr" else "en"
        val type = "restaurant"

        val apiService = this.retrofit.create(ApiService::class.java)

        return apiService.getRestaurants(type, location, radius, rankby, language, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS)
    }


}