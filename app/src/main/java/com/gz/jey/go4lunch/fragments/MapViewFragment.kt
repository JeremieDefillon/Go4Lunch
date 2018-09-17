package com.gz.jey.go4lunch.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.SetImageColor
import java.util.*

class MapViewFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val TAG = "MAP FRAGMENT"
    var mainActivity: MainActivity? = null
    private var mSupportMapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    var mView: View? = null

    private val DEFAULT_ZOOM = 14f


    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.map_view, container, false)
        return mView
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

        mMap!!.setOnMarkerClickListener(this)

        val locationButton= (mView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE)
        rlp.setMargins(0,0,50,50)

        try {
            if (mainActivity!!.mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
                mainActivity!!.setLoading(false, false)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(mainActivity!!.mLastKnownLocation!!.latitude, mainActivity!!.mLastKnownLocation!!.longitude), DEFAULT_ZOOM))

                var ico : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.self_marker)
                ico = SetImageColor.changeBitmapColor(ico, Color.RED)

                mMap!!.addMarker(MarkerOptions()
                        .position(mainActivity!!.mLastKnownLocation!!)
                        .title("Position")
                        .icon(BitmapDescriptorFactory.fromBitmap(ico)))

                val restaurants = ArrayList<Result>()
                restaurants.clear()
                restaurants.addAll(mainActivity!!.place!!.results)

                val iconIn : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.someone_in)
                val iconNone : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.none_in)

                for (rest : Result in restaurants){
                    val ico = if(rest.workmates !=null && rest.workmates.isNotEmpty())iconIn else iconNone

                    val location = LatLng(rest.geometry.location.lat, rest.geometry.location.lng)
                    mMap!!.addMarker(MarkerOptions()
                            .position(location)
                            .title(rest.name)
                            .icon(BitmapDescriptorFactory.fromBitmap(ico)))
                }
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                mainActivity!!.mLastKnownLocation = null
                mainActivity!!.getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
            mainActivity!!.getLocationPermission()
        }
    }

    override fun onMarkerClick(p0: Marker?) : Boolean {
        mainActivity!!.setLoading(false, true)
        for((index,value) in mainActivity!!.place!!.results.withIndex()){
            if(mainActivity!!.place!!.results[index].name == p0!!.title){
                mainActivity!!.restaurantID = mainActivity!!.place!!.results[index].placeId
                mainActivity!!.restaurantName = mainActivity!!.place!!.results[index].name
               break
            }
        }
        mainActivity!!.execRequest(mainActivity!!.CODE_DETAILS)
        return true
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