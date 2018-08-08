package com.gz.jey.go4lunch.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.gz.jey.go4lunch.R


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private val TAG = "MapsActivity"
    private val DEFAULT_ZOOM = 16f
    private val M_MAX_ENTRIES = 10
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 34

    private var mMap: GoogleMap? = null
    var lang = 1
    var mLocationPermissionGranted: Boolean = false
    var mGeoDataClient : GeoDataClient? = null
    var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mDefaultLocation: LatLng? = null
    private var mLastKnownLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        //AppCompatActivity.setSupportActionBar(mToolbar)

        onClickCurrentPosition()

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        onTabSelected(0)
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    fun onClickCurrentPosition(){
        val currentPositionButton = findViewById(R.id.current_position_button) as RelativeLayout
        currentPositionButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                getDeviceLocation()
            }
        })
    }

    internal fun onTabSelected(index : Int){
        val map_i = findViewById(R.id.map_button_img) as ImageView
        val map_t = findViewById(R.id.map_button_txt) as TextView
        val list_i = findViewById(R.id.list_button_img) as ImageView
        val list_t = findViewById(R.id.list_button_txt) as TextView
        val people_i = findViewById(R.id.people_button_img) as ImageView
        val people_t = findViewById(R.id.people_button_txt) as TextView
        val prim = ContextCompat.getColor(this, R.color.colorPrimary)
        val black = ContextCompat.getColor(this, R.color.colorBlack)

        map_i.setImageDrawable(changeDrawableColor(this, R.drawable.map, black))
        map_t.setText(resources.getStringArray(R.array.map)[lang])
        map_t.setTextColor(black)
        list_i.setImageDrawable(changeDrawableColor(this, R.drawable.list, black))
        list_t.setText(resources.getStringArray(R.array.restaurants)[lang])
        list_t.setTextColor(black)
        people_i.setImageDrawable(changeDrawableColor(this, R.drawable.people, black))
        people_t.setText(resources.getStringArray(R.array.workmates)[lang])
        people_t.setTextColor(black)

        when(index){
            0 -> {map_i.setColorFilter(prim)
                map_t.setTextColor(prim)}
            1 -> {list_i.setColorFilter(prim)
                list_t.setTextColor(prim)}
            2 -> {people_i.setColorFilter(prim)
                people_t.setTextColor(prim)}
        }
    }

    fun changeDrawableColor(context : Context, icon : Int, newColor : Int) : Drawable {
        val mDrawable = ContextCompat.getDrawable(context, icon)?.mutate() as Drawable
        mDrawable?.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
        return mDrawable
    }

    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                mMap!!.setMyLocationEnabled(true)
                mMap!!.getUiSettings().isMyLocationButtonEnabled = true
            } else {
                mMap!!.setMyLocationEnabled(false)
                mMap!!.getUiSettings().isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mFusedLocationProviderClient!!.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(mLastKnownLocation!!.latitude,
                                mLastKnownLocation!!.longitude), DEFAULT_ZOOM))

                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.exception)
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap!!.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
    }

    private fun showCurrentPlace() {
        if (mMap == null) {
            return
        }

        if (mLocationPermissionGranted!!) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            val placeResult : Task<PlaceLikelihoodBufferResponse> =
                    mPlaceDetectionClient!!.getCurrentPlace(null)
            placeResult.addOnCompleteListener(OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                fun onComplete(task : Task<PlaceLikelihoodBufferResponse>) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        val likelyPlaces : PlaceLikelihoodBufferResponse = task.getResult()

                        // Set the count, handling cases where less than 5 entries are returned.
                        val count : Int
                        if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getCount();
                        } else {
                            count = M_MAX_ENTRIES
                        }

                        var i = 0
                        val mLikelyPlaceNames = arrayOfNulls<String>(count)
                        val mLikelyPlaceAddresses = arrayOfNulls<String>(count)
                        val mLikelyPlaceAttributions= arrayOfNulls<String>(count)
                        val mLikelyPlaceLatLngs = arrayOfNulls<LatLng>(count)

                        for (placeLikelihood in likelyPlaces) {
                            // Build a list of likely places to show the user.

                            mLikelyPlaceNames[i] = placeLikelihood.getPlace().getName() as String?
                            mLikelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress() as String?
                            mLikelyPlaceAttributions[i] = placeLikelihood.getPlace().getAttributions() as String?
                            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng()

                            i++
                            if (i > (count - 1)) {
                                break
                            }
                        }

                        // Release the place likelihood buffer, to avoid memory leaks.
                        likelyPlaces.release();

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        //openPlacesDialog();

                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap!!.addMarker(MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(this!!.mDefaultLocation!!)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
}