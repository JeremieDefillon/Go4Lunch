package com.gz.jey.go4lunch.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
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
import com.gz.jey.go4lunch.models.User
import com.gz.jey.go4lunch.utils.ApiStreams
import com.gz.jey.go4lunch.utils.CheckIfTest
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    // FRAGMENTS
    private val TAG = "MainActivity"
    private var signInFragment: SignInFragment? = null
    private var mapViewFragment: MapViewFragment? = null
    private var restaurantsFragment: RestaurantsFragment? = null
    private var workmatesFragment: WorkmatesFragment? = null
    private var detailsFragment: RestaurantDetailsFragment? = null

    // FOR PERMISSIONS
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 34
    var mLocationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_PHONE_CALL = 69
    private var mPhoneCallPermissionGranted = false

    // TASK CODE
    private val SIGN_OUT_TASK = 99
    val GPS = 10
    private val RESTAURANTS = 34
    val CONTACTS = 37
    val SEARCH_FOR = 44

    // FOR POSITION
    var mGeoDataClient : GeoDataClient? = null
    var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    var mLastKnownLocation: LatLng? = null

    // FOR DESIGN
    private var toolMenu : Menu? = null
    var fragmentContainer : FrameLayout? = null
    private var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var searchBar: LinearLayout? = null
    var bottom: BottomNavigationView? = null
    private var navigationView: NavigationView? = null
    private var accountPicture : ImageView? = null
    var loading : FrameLayout? = null

    // FOR DATA
    var user : User? = null
    var contacts : ArrayList<Contact> = ArrayList()
    var place : Place? = null
    private var tab = 0
    private var username : String?= null
    var email : String?= null
    private var number : String? = null
    var lang = 1

    // FOR RESTAURANT SELECTOR
    var restaurantID: String? = null
    var restaurantName: String? = null

    var disposable : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        loading = findViewById(R.id.loading)
        setLoading(true, true)
        //loading!!.visibility = VISIBLE
        fragmentContainer = findViewById(R.id.fragmentContainer)
        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        val settings : FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
        .setTimestampsInSnapshotsEnabled(true)
        .build()
        firestore.firestoreSettings = settings

        if (savedInstanceState == null)
            initActivity()
    }

    private fun initActivity(){
        Log.d("START INIT", "ACTIVITY")
        if(!CheckIfTest.isRunningTest("NavDrawerTest"))
            when(isCurrentUserLogged()){
                true -> {
                    checkUserInFirestore()
                    this.setLocalisationData()
                    this.configureToolBar()
                    this.configureBottomBar()
                    this.setDrawerLayout()
                    this.setNavigationView()
                    tab = if(intent.extras.containsKey("Index"))
                        intent.extras.getInt("Index")
                    else
                        1
                    execRequest(GPS)
                }
                false -> {setFragment(0)}
                else -> setFragment(0)
            }
        else{
            this.setDrawerLayout()
            this.setNavigationView()
        }
    }

    private fun setLocalisationData(){
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(applicationContext)
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(applicationContext)
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    // Configure Toolbar
    private fun configureToolBar() {
        this.toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        searchBar = findViewById(R.id.searchBar)
        val rlp= searchBar!!.layoutParams as (LinearLayout.LayoutParams)
        // position center with margin
        //rlp.addRule(LinearLayout.ALIGN_PARENT_TOP,0)
        //rlp.addRule(LinearLayout.ALIGN_PARENT_BOTTOM,LinearLayout.TRUE)
        rlp.height = (calculateActionBar()*0.7f).toInt()
        rlp.setMargins(20,20,20,20)
    }

   // Configure SearchBar
    private fun configureSearchBar(tab : Int) {
        val searchText = searchBar!!.findViewById<EditText>(R.id.search_txt)
        val backSearch = searchBar!!.findViewById<ImageButton>(R.id.back_search)
        val speechSearch = searchBar!!.findViewById<ImageButton>(R.id.speech_search)

        if(tab == 3) searchText.hint = getString(R.string.search_workmates)
        else searchText.hint = getString(R.string.search_restaurants)

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) { }
            override fun onTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) { }
            override fun afterTextChanged(editable : Editable) {
                if(tab==3){
                    val contactsFetched : ArrayList<Contact> = ArrayList()
                    contactsFetched.clear()
                    for(c in contacts){
                        if(c.username.contains(editable, true)){
                            contactsFetched.add(c)
                        }
                    }
                    workmatesFragment!!.updateUI(contactsFetched)
                }else{
                    execRequest(SEARCH_FOR)
                }
            }
        })

       backSearch.setOnClickListener {
           toolbar!!.visibility = VISIBLE
           searchBar!!.visibility = GONE}

       speechSearch.setOnClickListener{

       }
   }

    /**
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolMenu = menu
        // Inflate the menu and add it to the Toolbar
        menuInflater.inflate(R.menu.menu_toolbar, toolMenu)
        return true
    }

    /**
     * @param item MenuItem
     * @return boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        // Handle item selection
        return when (id) {
            R.id.search_on -> {
                toolbar!!.visibility = GONE
                searchBar!!.visibility = VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Configure BottomBar
    private fun configureBottomBar() {
        this.bottom = this.findViewById(R.id.bottom_menu)
        this.bottom!!.setOnNavigationItemSelectedListener { item ->
            setLoading(false, true)
            when (item.itemId) {
                R.id.map_button -> {
                    tab = 1
                    execRequest(GPS)}
                R.id.restaurants_button -> {
                    tab = 2
                    execRequest(RESTAURANTS)
                }
                R.id.workmates_button -> {
                    tab = 3
                    execRequest(CONTACTS)
                }
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
            Glide.with(this)
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
            R.id.restaurant_menu -> {
                if(!user!!.whereEatID.isEmpty()){
                    restaurantID = user!!.whereEatID
                    restaurantName = user!!.whereEatName
                    setFragment(4)
                }else{
                    popupmsg(getString(R.string.none_restaurant))
                }
            }
            R.id.settings -> setSettings()
            R.id.power_settings -> disconnect()
        }
        this.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
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
     * @param index Int
     * Change Fragment
     */
    fun setFragment(index: Int){
        var fragment : Fragment? = null
        invalidateOptionsMenu()
        when(index){
            0->{
                this.signInFragment = SignInFragment.newInstance(this)
                fragment=this.signInFragment
            }
            1->{tab=index
                this.mapViewFragment = MapViewFragment.newInstance(this)
                fragment = this.mapViewFragment
            }
            2->{tab=index
                this.restaurantsFragment = RestaurantsFragment.newInstance(this)
                fragment = this.restaurantsFragment
            }
            3->{
                tab=index
                this.workmatesFragment = WorkmatesFragment.newInstance(this)
                fragment = this.workmatesFragment
            }
            4->{
                Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.back_button)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                toolbar!!.setNavigationOnClickListener {
                    setFragment(tab)
                }
                this.detailsFragment = RestaurantDetailsFragment.newInstance(this)
                bottom!!.visibility = GONE
                setFrameLayoutMargin(false)
                fragment = this.detailsFragment
            }
        }

        if(index!=0 && index!=4){
            this.configureSearchBar(tab)
            Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
            setDrawerLayout()
            bottom!!.visibility = VISIBLE
            setFrameLayoutMargin(true)
        }

        this.supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun popupmsg(msg : String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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

    // request that create user in firestore
    private fun checkUserInFirestore(){
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

    @Suppress("UNCHECKED_CAST")
    private fun loadData(data : DocumentSnapshot){
        user=User(
                data["uid"] as String,
                data["username"] as String,
                data["mail"] as String,
                data["urlPicture"] as String,
                data["whereEatID"] as String,
                data["whereEatName"] as String,
                data["restLiked"] as ArrayList<String> )

        contacts = ArrayList()
        contacts.clear()
        UserHelper.getUsersCollection().get().addOnSuccessListener {
            for(contact in it.documents){

                if(contact.get("uid").toString() != getCurrentUser()!!.uid) {
                    val uid = contact.get("uid").toString()
                    val username = contact.get("username").toString()
                    val urlPicture = contact.get("urlPicture").toString()
                    val whereEatID= contact.get("whereEatID").toString()
                    val whereEatName= contact.get("whereEatName").toString()
                    val restLiked= contact.get("restLiked") as ArrayList<String>

                    val cntc = Contact(uid, username, urlPicture, whereEatID, whereEatName, restLiked)
                    contacts.add(cntc)
                }
            }

            setAllContacts()
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
            UserHelper.createUser(uid, username!!, mail!!, urlPicture, whereEat, whereEat, restLiked).addOnFailureListener(this.onFailureListener())
            checkUserInFirestore()
        }
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

    /**
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent
     */
    fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent) {
        val response = IdpResponse.fromResultIntent(data)
        if (requestCode == signInFragment?.rcSignIn ?: Int) {
            if (resultCode == Activity.RESULT_OK) {
                // SUCCESS
                initActivity()
                popupmsg(getString(R.string.connection_succeed))
            } else { // ERRORS
                when {
                    response == null -> popupmsg(getString(R.string.error_authentication_canceled))
                    response.error?.equals(ErrorCodes.NO_NETWORK) ?: (false) -> popupmsg(getString(R.string.error_no_internet))
                    response.error?.equals(ErrorCodes.UNKNOWN_ERROR) ?: (false) -> popupmsg(getString(R.string.error_unknown_error))
                }
            }
        }
    }

    // Create OnCompleteListener called after tasks ended
    private fun updateUIAfterRESTRequestsCompleted(origin: Int): OnSuccessListener<Void> {
        setLoading(true, true)
        return OnSuccessListener {
            when (origin) {
                SIGN_OUT_TASK -> {
                    initActivity()}
                else -> {
                }
            }
        }
    }

    fun execRequest(req : Int) {
        when (req) {
            GPS ->{
                getDeviceLocation()
            }
            RESTAURANTS -> {
                if (mLastKnownLocation != null){
                    disposable = ApiStreams.streamFetchRestaurants(getString(R.string.google_maps_key), mLastKnownLocation!!, lang)
                        .subscribeWith(object : DisposableObserver<Place>() {
                            override fun onNext(place: Place) {
                                setAllRestaurants(place)
                            }

                            override fun onError(e: Throwable) {
                                Log.e("MAP RX", e.toString())
                            }

                            override fun onComplete() {}
                        })
                }else{
                    execRequest(GPS)
                }
            }
            CONTACTS -> {
                checkUserInFirestore()
            }
            SEARCH_FOR ->{
                disposable = ApiStreams.streamFetchRestaurants(getString(R.string.google_maps_key), mLastKnownLocation!!, lang)
                    .subscribeWith(object : DisposableObserver<Place>() {
                        override fun onNext(place: Place) {
                            setAllRestaurants(place)
                        }

                        override fun onError(e: Throwable) {
                            Log.e("MAP RX", e.toString())
                        }

                        override fun onComplete() {}
                    })
            }
        }
    }

    fun setAllRestaurants(place : Place){
        this.place = place
        if(contacts.size!=0)
            setAllContacts()
        else
            execRequest(CONTACTS)
    }

    private fun setAllContacts(){
        if(place!=null){
            for(r in place!!.results){
                if(user!!.restLiked.contains(r.placeId))
                    r.liked++
                for(c in contacts){
                    if(c.restLiked.contains(r.placeId))
                        r.liked++
                }
            }

            for(c in contacts){
                if(!c.whereEatID.isEmpty()){
                    for(r in place!!.results){
                        if(r.placeId==c.whereEatID) {
                            if (!r.workmates.contains(c)) {
                                r.workmates.add(c)
                                break
                            }
                        }
                    }
                }
            }

            setFragment(tab)
        }else{
            execRequest(RESTAURANTS)
        }
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        mFusedLocationProviderClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    Log.d(TAG, " TASK DEVICE LOCATION SUCCESS")
                    mLastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                    execRequest(RESTAURANTS)
                } else {
                    Log.w("MAP LOCATION", "getLastLocation:exception", task.exception)
                    Log.e("MAP LOCATION", "Exception: %s", task.exception)
                    // Prompt the user for permission.
                    getLocationPermission()
                }
            }
    }

    @SuppressLint("MissingPermission")
    fun callTo(number: String){
        if (mPhoneCallPermissionGranted) {
            //mainActivity!!.callTo(details.result.internationalPhoneNumber)
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel: $number")
            this.startActivity(callIntent)
        } else {
            this.number = number
            getPhoneCallPermission()
        }
    }

    fun openWebsite(url:String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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

    private fun setFrameLayoutMargin(marged : Boolean){
        val marge = calculateActionBar()
        val bottom = if(marged) marge else 0
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(0, marge, 0, bottom)
        fragmentContainer!!.layoutParams = layoutParams
    }

    private fun calculateActionBar() : Int {
        // Calculate ActionBar height
        val tv = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }else
            0
    }

    // HANDLE PERMISSIONS

    fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun getPhoneCallPermission() {
        /*
     * Request phone call permission.
     * The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(applicationContext,
                        android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            mPhoneCallPermissionGranted = true
            callTo(number!!)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    PERMISSIONS_REQUEST_PHONE_CALL)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        mPhoneCallPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
            PERMISSIONS_REQUEST_PHONE_CALL -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhoneCallPermissionGranted = true
                    callTo(number!!)
                }
            }
        }
    }

    fun setLoading(type : Boolean, onoff : Boolean){
        val fllp : DrawerLayout.LayoutParams = loading!!.layoutParams as DrawerLayout.LayoutParams
        if(onoff){
            if(type){
                fllp.setMargins(0,0,0,0)
                loading!!.background = resources.getDrawable(R.drawable.login_screen)
                loading!!.findViewById<TextView>(R.id.title_loading).setTextColor(resources.getColor(R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.label_loading).setTextColor(resources.getColor(R.color.colorWhite))
            }else{
                val size = calculateActionBar()
                fllp.setMargins(0,size,0,size)
                loading!!.setBackgroundColor(resources.getColor(R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.title_loading).setTextColor(resources.getColor(R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.label_loading).setTextColor(resources.getColor(R.color.colorPrimaryDark))
            }
            loading!!.visibility = VISIBLE
        }
        else
            loading!!.visibility = GONE
    }

}

