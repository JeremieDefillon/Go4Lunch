package com.gz.jey.go4lunch.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Place
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiStreams
import com.gz.jey.go4lunch.utils.SetBottomMenuTab
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class MapViewFragment : Fragment(), OnMapReadyCallback{

    private val TAG = "MAP FRAGMENT"
    var mainActivity: MainActivity? = null
    var mSupportMapFragment: SupportMapFragment? = null
    var mMap: GoogleMap? = null

    private val DEFAULT_ZOOM = 16f
    private val M_MAX_ENTRIES = 10
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 34

    var lang = 1
    var mLocationPermissionGranted: Boolean = false
    var mGeoDataClient : GeoDataClient? = null
    var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mDefaultLocation: LatLng? = null
    private var mLastKnownLocation: LatLng? = null
    var disposable : Disposable? = null

    private var currentPositionMarker : MarkerOptions? = null
    private var currentLocationMarker : Marker? = null

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mSupportMapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG,"ON MAP READY")
        mMap = map
        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_style))

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(mainActivity!!.applicationContext)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(mainActivity!!.applicationContext)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity!!.applicationContext)

        setOnClickCurrentPosition()
        updateLocationUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.disposeWhenDestroy()
    }

    private fun setOnClickCurrentPosition(){
        val currentPositionButton = mainActivity!!.findViewById<RelativeLayout>(R.id.current_position_button)
        currentPositionButton.setOnClickListener { getDeviceLocation() }
    }


    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(mainActivity!!.applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(mainActivity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
                getDeviceLocation()
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
            mFusedLocationProviderClient.lastLocation
            .addOnCompleteListener(mainActivity!!) { task ->
                if (task.isSuccessful && task.result != null) {
                    Log.d(TAG, " TASK SUCCESS")
                    mLastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                    showCurrentPlace()
                } else {
                    Log.w("MAP LOCATION", "getLastLocation:exception", task.exception)
                    Log.e("MAP LOCATION", "Exception: %s", task.exception)
                    // Prompt the user for permission.
                    getLocationPermission()
                }
            }
    }

    private fun showCurrentPlace() {
        Log.d(TAG, " CURRENT PLACE")
        if (mMap == null)
            return

        if (mLocationPermissionGranted) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude), DEFAULT_ZOOM))

            var ico : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.self_marker)
            ico = SetBottomMenuTab.changeBitmapColor(ico, Color.RED)

            mMap!!.addMarker(MarkerOptions()
                    .position(mLastKnownLocation!!)
                    .title("Position")
                    .icon(BitmapDescriptorFactory.fromBitmap(ico)))

            executeHttpRequestWithRetrofit("place")
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.")

            // Add a default marker, because the user hasn't selected a place.
            mMap!!.addMarker(MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation!!)
                    .snippet(getString(R.string.default_info_snippet)))

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private fun executeHttpRequestWithRetrofit(req : String) {
        when (req) {
            "place" -> {
                val location : LatLng =
                        if(mLastKnownLocation == null) mDefaultLocation!!
                        else mLastKnownLocation!!
                        disposable = ApiStreams.streamFetchRestaurants(getString(R.string.google_maps_key), location, lang)
                        .subscribeWith(object : DisposableObserver<Place>(){
                            override fun onNext(place: Place) {
                                updateRestaurantsMarkers(place)
                            }

                            override fun onError(e: Throwable) {
                                Log.e("MAP RX", e.toString())
                            }

                            override fun onComplete() {
                            }
                        })
            }
        }
    }

    private fun disposeWhenDestroy() {
        if (this.disposable != null && !this.disposable!!.isDisposed)
            this.disposable!!.dispose()
    }

    private fun updateRestaurantsMarkers(place : Place){
        Log.d("PLACE" , place.status.toString())
        val restaurants = ArrayList<Result>()
        restaurants.clear()
        restaurants.addAll(place.results)

        val iconIn : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.someone_in)
        val iconNone : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.none_in)

        for (rest : Result in restaurants){
            val ico = if(rest.workmates!=null && rest.workmates.isNotEmpty())iconIn else iconNone

            val location = LatLng(rest.geometry.location.lat, rest.geometry.location.lng)
            mMap!!.addMarker(MarkerOptions()
                    .position(location)
                    .title(rest.name)
                    .icon(BitmapDescriptorFactory.fromBitmap(ico)))
        }
    }

    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new SignInFragment()
         */
        fun newInstance(mainActivity : MainActivity): MapViewFragment {
            val fragment = MapViewFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }
}