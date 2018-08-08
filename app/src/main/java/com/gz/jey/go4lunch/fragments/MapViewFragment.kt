package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity


class MapViewFragment : Fragment(), OnMapReadyCallback{


    var mainActivity: MainActivity? = null
    var mapView: MapView? = null
    var mMap: GoogleMap? = null

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

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_view, container, false)
        // Gets the MapView from the XML layout and creates it
        mapView = view as MapView
        mapView?.onCreate(savedInstanceState)

        // Gets to GoogleMap from the MapView and does initialization stuff
        var mapFragment : SupportMapFragment?=null
        mapFragment = fragmentManager?.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap : GoogleMap?) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

        /**
     * Destroy this Fragment
     */
    override fun onDestroy() {
        super.onDestroy()
    }
}