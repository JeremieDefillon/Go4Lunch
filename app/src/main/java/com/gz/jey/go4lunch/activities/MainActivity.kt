package com.gz.jey.go4lunch.activities

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.fragments.MapViewFragment
import com.gz.jey.go4lunch.fragments.RestaurantsFragment
import com.gz.jey.go4lunch.fragments.SignInFragment
import com.gz.jey.go4lunch.fragments.WorkmatesFragment
import com.gz.jey.go4lunch.utils.CheckIfTest
import com.gz.jey.go4lunch.utils.SetBottomMenuTab
import io.reactivex.disposables.Disposable


class MainActivity : AppCompatActivity(){

    private val TAG = "MainActivity"
    var signInFragment: SignInFragment? = null
    private var mapViewFragment: MapViewFragment? = null
    private var restaurantsFragment: RestaurantsFragment? = null
    private var workmatesFragment: WorkmatesFragment? = null

    // FOR DESIGN
    var coordinatorLayout: CoordinatorLayout? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null

    // FOR DATA
    var lang = 1
    var mDefaultLocation: LatLng? = null
    var mLastKnownLocation: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        this.setBottomButtonClick()
        this.setDrawerLayout()
        this.setNavigationView()

        if(!CheckIfTest.isRunningTest("NavDrawerTest"))
            when(isCurrentUserLogged()){
                true -> {
                    if (savedInstanceState == null) {
                    when(intent.extras.getInt("Index")) {
                        0 -> setMapViewFragment()
                        1 -> setRestaurantsFragment()
                        2 -> setWorkmatesFragment()
                        else -> setMapViewFragment()
                    }
                }}
                false -> setSignInFragment()
                else -> setSignInFragment()
            }
    }

    /**
     * Configure Bottom Bar Buttons Click
     */
    private fun setBottomButtonClick(){
        val map = findViewById<RelativeLayout>(R.id.map_button)
        val restaurants = findViewById<RelativeLayout>(R.id.restaurants_button)
        val workmates = findViewById<RelativeLayout>(R.id.workmates_button)

        map.setOnClickListener{setMapViewFragment()}
        restaurants.setOnClickListener{setRestaurantsFragment()}
        workmates.setOnClickListener {setWorkmatesFragment()}
    }


    /**
     * Configure Drawer Layout
     */
    private fun setDrawerLayout() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.activity_main_drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()
    }

    /**
     * Configure NavigationView
     */
    private fun setNavigationView() {
        navigationView = findViewById(R.id.activity_main_nav_view)
        navigationView!!.menu.clear()
        menuInflater.inflate(R.menu.menu_nav_drawer, navigationView!!.menu)
        val menu = navigationView!!.menu

        //navigationView!!.setNavigationItemSelectedListener(this)
    }

    /**
     * Set Sign In
     */
    private fun setSignInFragment(){
        this.signInFragment = SignInFragment.newInstance(this)
        this.moveFragment(this.signInFragment!!)
    }

    /**
     * Set MapView
     */
    private fun setMapViewFragment(){
        Log.d(TAG,"SET MAP VIEW FRAGMENT")
        this.mapViewFragment = MapViewFragment.newInstance(this)

        SetBottomMenuTab.onTabSelected(this, this, 0)
        this.moveFragment(this.mapViewFragment!!)
    }

    /**
     * Set Restaurants
     */
    private fun setRestaurantsFragment(){
        this.restaurantsFragment = RestaurantsFragment.newInstance(this)
        SetBottomMenuTab.onTabSelected(this, this, 1)
        this.moveFragment(this.restaurantsFragment!!)
    }

    /**
     * Set Workmates
     */
    private fun setWorkmatesFragment(){
        this.workmatesFragment = WorkmatesFragment.newInstance(this)
        SetBottomMenuTab.onTabSelected(this, this, 2)
        this.moveFragment(this.workmatesFragment!!)
    }

    /**
     * @param fragment Fragment
     * Add or Change Fragment
     */
    private fun moveFragment(fragment: Fragment){
        this.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
    }

    /**
     * @param coordinatorLayout CoordinatorLayout
     * @param message String
     */
    internal fun showSnackBar(message: String) {
        if(message == getString(R.string.connection_succeed))
            setMapViewFragment()

        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout)
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_SHORT).show()
    }

    @Nullable
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

}