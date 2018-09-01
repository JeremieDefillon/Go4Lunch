package com.gz.jey.go4lunch.activities

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.api.UserHelper
import com.gz.jey.go4lunch.fragments.*
import com.gz.jey.go4lunch.models.Contact
import com.gz.jey.go4lunch.models.Place
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.models.User
import com.gz.jey.go4lunch.utils.CheckIfTest
import com.gz.jey.go4lunch.views.GlideApp
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    companion object {
        val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    private val TAG = "MainActivity"
            val ESC = "¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤"
    var signInFragment: SignInFragment? = null
    private var mapViewFragment: MapViewFragment? = null
    private var restaurantsFragment: RestaurantsFragment? = null
    private var workmatesFragment: WorkmatesFragment? = null
    private var detailsFragment: RestaurantDetailsFragment? = null

    private val SIGN_OUT_TASK = 99
    private val SIGN_IN_TASK = 98

    // FOR DESIGN
    var toolMenu : Menu? = null
    var coordinatorLayout: CoordinatorLayout? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var bottom: BottomNavigationView? = null
    var navigationView: NavigationView? = null
    var accountPicture : ImageView? = null

    // FOR DATA
    var user : User? = null
    var contacts : ArrayList<Contact> = ArrayList()
    var place : Place? = null
    var tab = 0
    var username : String?= null
    var email : String?= null

    var lang = 1
    var mDefaultLocation: LatLng? = null
    var mLastKnownLocation: LatLng? = null
    var restaurantID: String? = null

    var searchMenu = false


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        val settings : FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
        .setTimestampsInSnapshotsEnabled(true)
        .build()
        firestore.firestoreSettings = settings

        if (savedInstanceState == null)
            initActivity()
    }

    private fun initActivity(){
        if(!CheckIfTest.isRunningTest("NavDrawerTest"))
            when(isCurrentUserLogged()){
                true -> {
                    checkUserInFirestore()
                    this.configureToolBar()
                    this.configureBottomBar()
                    this.setDrawerLayout()
                    this.setNavigationView()
                        when(intent.extras.getInt("Index")) {
                            0 -> setMapViewFragment()
                            1 -> setRestaurantsFragment()
                            2 -> setWorkmatesFragment()
                            else -> setMapViewFragment()
                        }
                }
                false -> {setSignInFragment()}
                else -> setSignInFragment()
            }
        else{
            this.setDrawerLayout()
            this.setNavigationView()
        }
    }

    // Configure Toolbar
    private fun configureToolBar() {
        this.toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    /**
     * @param menu Menu
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolMenu = menu

        // Inflate the menu and add it to the Toolbar
        menuInflater.inflate(R.menu.menu_activity_main, toolMenu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (toolMenu!!.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }


    // Configure BottomBar
    private fun configureBottomBar() {
        this.bottom = findViewById(R.id.bottom_menu)
        this.bottom!!.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.map_button -> setMapViewFragment()
                R.id.restaurants_button -> setRestaurantsFragment()
                R.id.workmates_button -> setWorkmatesFragment()
            }
            true
        }
    }

    /**
     * Configure Drawer Layout
     */
    private fun setDrawerLayout() {
        drawerLayout = findViewById(R.id.activity_main_drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
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

        accountPicture = navigationView!!.getHeaderView(0).findViewById(R.id.account_picture)

        this.getCurrentUser()!!.photoUrl

        //Get picture URL from Firebase
        if (this.getCurrentUser()!!.photoUrl != null) {
            GlideApp.with(this)
                    .load(this.getCurrentUser()!!.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(accountPicture!!)
        }

        //Get email & username from Firebase
        email = if (TextUtils.isEmpty(this.getCurrentUser()!!.email))
            getString(R.string.no_email_found) else this.getCurrentUser()!!.email
        username = if (TextUtils.isEmpty(this.getCurrentUser()!!.displayName))
            getString(R.string.no_username_found) else this.getCurrentUser()!!.displayName

        navigationView!!.getHeaderView(0).findViewById<TextView>(R.id.account_name).text = username
        navigationView!!.getHeaderView(0).findViewById<TextView>(R.id.account_mail).text = email

        navigationView!!.setNavigationItemSelectedListener(this)
    }

    /**
     * @param item MenuItem
     * @return true
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle Navigation Item Click
        item.isChecked = true
        when (item.itemId) {
            R.id.restaurant_menu -> setDetailsRestaurant()
            R.id.settings -> setSettings()
            R.id.power_settings -> disconnect()
        }
        this.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
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
        tab=0
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        Log.d(TAG,"SET MAP VIEW FRAGMENT")
        this.mapViewFragment = MapViewFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        this.moveFragment(this.mapViewFragment!!)
    }

    /**
     * Set Restaurants
     */
    private fun setRestaurantsFragment(){
        tab = 1
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        this.restaurantsFragment = RestaurantsFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        this.moveFragment(this.restaurantsFragment!!)
    }

    /**
     * Set Workmates
     */
    private fun setWorkmatesFragment(){
        tab=2
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        this.workmatesFragment = WorkmatesFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        this.moveFragment(this.workmatesFragment!!)
    }

    /**
     * Set Restaurants
     */
    fun setDetailsRestaurant(){
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.back_button)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener {
            when(tab){
                0->setMapViewFragment()
                1->setRestaurantsFragment()
                2->setWorkmatesFragment()
            }
        }
        this.detailsFragment = RestaurantDetailsFragment.newInstance(this)
        bottom!!.visibility = GONE
        this.moveFragment(this.detailsFragment!!)
    }

    /**
     * Set Settings
     */
    private fun setSettings(){
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.back_button)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

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
        coordinatorLayout = this.findViewById(R.id.main_activity_coordinator_layout)
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Set Settings
     */
    private fun disconnect(){
        if (this.getCurrentUser() != null) {
            AuthUI.getInstance()
            .signOut(this)
            .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK))
        }
    }

    // --------------------
    // REST REQUEST
    // --------------------

    // 1 - Http request that create user in firestore
    fun checkUserInFirestore(){
        if (this.getCurrentUser() != null){
            val uid = getCurrentUser()!!.uid

            UserHelper.getUser(uid).addOnSuccessListener {
                if(it["uid"].toString()==uid)
                    loadData(it)
                else
                    createUserInFirestore()
            }
        }
    }

    private fun loadData(data : DocumentSnapshot){
        user=User(
                data["uid"] as String,
                data["username"] as String,
                data["mail"] as String,
                data["urlPicture"] as String,
                data["whereEat"] as String,
                data["restLiked"] as ArrayList<String> )

        contacts = ArrayList()
        UserHelper.getUsersCollection().get().addOnSuccessListener {
            for(contact in it.documents){
                if(contact.get("uid").toString() != getCurrentUser()!!.uid) {
                    val uid = contact.get("uid").toString()
                    val username = contact.get("username").toString()
                    val mail = contact.get("mail").toString()
                    val urlPicture = contact.get("urlPicture").toString()
                    val whereEat = contact.get("whereEat").toString()
                    val cntc = Contact(uid, username, mail, urlPicture, whereEat)
                    contacts.add(cntc)
                }
            }

            if(tab==2)
                workmatesFragment!!.initList()
        }
    }

    // Http request that create user in firestore
    private fun createUserInFirestore(){
        if (this.getCurrentUser() != null){
            val mail = getCurrentUser()!!.email
            val urlPicture = if(getCurrentUser()!!.photoUrl != null) getCurrentUser()!!.photoUrl.toString() else ""
            val username = getCurrentUser()!!.displayName
            val uid = getCurrentUser()!!.uid
            val whereEat = ""
            val restLiked : ArrayList<String> = ArrayList()
            UserHelper.createUser(uid, username!!, mail!!, urlPicture, whereEat, restLiked).addOnFailureListener(this.onFailureListener())
            checkUserInFirestore()
        }
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

    // Create OnCompleteListener called after tasks ended
    fun updateUIAfterRESTRequestsCompleted(origin: Int): OnSuccessListener<Void> {
        return OnSuccessListener {
            when (origin) {
                SIGN_IN_TASK -> initActivity()
                SIGN_OUT_TASK -> initActivity()
                else -> {
                }
            }
        }
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    private fun onFailureListener()
    : OnFailureListener {
        return OnFailureListener {
            Toast.makeText(applicationContext, getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show()
        }
    }

}
