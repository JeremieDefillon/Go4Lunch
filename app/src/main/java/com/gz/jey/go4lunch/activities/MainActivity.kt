package com.gz.jey.go4lunch.activities

import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.TextView
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.fragments.MapViewFragment
import com.gz.jey.go4lunch.fragments.SignInFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.gz.jey.go4lunch.utils.CheckIfTest
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat.getDrawable


class MainActivity : AppCompatActivity() {

    var signInFragment: SignInFragment? = null
    var mapViewFragment: MapViewFragment? = null

    // FOR DESIGN
    var coordinatorLayout: CoordinatorLayout? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var navigationView: NavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        this.setDrawerLayout()
        this.setNavigationView()

        if(!CheckIfTest.isRunningTest("NavDrawerTest"))
            this.setSignInFragment()
    }

    /**
     * Configure Drawer Layout
     */
    private fun setDrawerLayout() {
        toolbar = findViewById(R.id.activity_main_toolbar)
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
        navigationView!!.getMenu().clear()
        menuInflater.inflate(R.menu.menu_nav_drawer, navigationView!!.getMenu())
        val menu = navigationView!!.getMenu()


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
    internal fun setMapViewFragment(){
        this.mapViewFragment = MapViewFragment.newInstance(this)
        this.moveFragment(this.mapViewFragment!!)
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
        if(message == getString(R.string.connection_succeed)){
            setMapViewFragment()
        }
        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout)
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_SHORT).show()
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


        map_i.setColorFilter(black, PorterDuff.Mode.SRC_IN)
        map_t.setTextColor(black)
        list_i.setColorFilter(black, PorterDuff.Mode.SRC_IN)
        list_t.setTextColor(black)
        people_i.setColorFilter(black, PorterDuff.Mode.SRC_IN)
        people_t.setTextColor(black)

        if(index == 0){
            map_i.setColorFilter(prim)
            map_t.setTextColor(prim)
        }
    }

    @Nullable
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

}

