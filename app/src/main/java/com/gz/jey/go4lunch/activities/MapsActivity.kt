package com.gz.jey.go4lunch.activities

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gz.jey.go4lunch.R

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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
        map_t.setTextColor(black)
        list_i.setImageDrawable(changeDrawableColor(this, R.drawable.list, black))
        list_t.setTextColor(black)
        people_i.setImageDrawable(changeDrawableColor(this, R.drawable.people, black))
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
}