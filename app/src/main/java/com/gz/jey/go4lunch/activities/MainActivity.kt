package com.gz.jey.go4lunch.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.*
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
import com.gz.jey.go4lunch.utils.CheckIfTest
import com.mancj.materialsearchbar.MaterialSearchBar
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    // FOR PERMISSIONS

    private val PERMISSIONS_REQUEST_PHONE_CALL = 69
    var mPhoneCallPermissionGranted = false
    private var number : String? = null


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
    var fragmentContainer : FrameLayout? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var searchBar: LinearLayout? = null
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
    var restaurantName: String? = null

    var searchMenu = false


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
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

        if(tab == 2) searchText.hint = getString(R.string.search_workmates)
        else searchText.hint = getString(R.string.search_restaurants)

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) {

            }

            override fun onTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) {

            }

            override fun afterTextChanged(editable : Editable) {
                if(tab==2){
                    val contactsFetched : ArrayList<Contact> = ArrayList()
                    for(c in contacts){
                        if(c.username.contains(editable)){
                            contactsFetched.add(c)
                        }
                    }
                    workmatesFragment!!.updateUI(contactsFetched)
                }else{

                }
            }
        })

       backSearch.setOnClickListener {
           toolbar!!.visibility = VISIBLE
           searchBar!!.visibility = GONE}
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
        when (id) {
            R.id.search_on -> {
                toolbar!!.visibility = GONE
                searchBar!!.visibility = VISIBLE
                return true
            }
            /*R.id.search_off -> {
                toolbar!!.visibility = VISIBLE
                searchBar!!.visibility = GONE
                return true
            }*/
            else -> return super.onOptionsItemSelected(item)
        }
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
                if(user!!.whereEatID!=null && !user!!.whereEatID.isEmpty()){
                    restaurantID = user!!.whereEatID
                    restaurantName = user!!.whereEatName
                    setDetailsRestaurant()
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
        this.configureSearchBar(tab)
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        Log.d(TAG,"SET MAP VIEW FRAGMENT")
        this.mapViewFragment = MapViewFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        setFrameLayoutMargin(true)
        this.moveFragment(this.mapViewFragment!!)
    }

    /**
     * Set Restaurants
     */
    private fun setRestaurantsFragment(){
        tab = 1
        this.configureSearchBar(tab)
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        this.restaurantsFragment = RestaurantsFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        setFrameLayoutMargin(true)
        this.moveFragment(this.restaurantsFragment!!)
    }

    /**
     * Set Workmates
     */
    private fun setWorkmatesFragment(){
        tab=2
        this.configureSearchBar(tab)
        invalidateOptionsMenu()
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
        setDrawerLayout()
        this.workmatesFragment = WorkmatesFragment.newInstance(this)
        bottom!!.visibility = VISIBLE
        setFrameLayoutMargin(true)
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
        setFrameLayoutMargin(false)
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
        UserHelper.getUsersCollection().get().addOnSuccessListener {
            for(contact in it.documents){

                if(contact.get("uid").toString() != getCurrentUser()!!.uid) {
                    val uid = contact.get("uid").toString()
                    val username = contact.get("username").toString()
                    val urlPicture = contact.get("urlPicture").toString()
                    val whereEatID= contact.get("whereEatID").toString()
                    val whereEatName= contact.get("whereEatName").toString()
                    val restLiked= arrayListOf(contact.get("restLiked").toString())

                    val cntc = Contact(uid, username, urlPicture, whereEatID, whereEatName, restLiked)
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
        Log.d("MARGIN ACTION BAR", marge.toString())
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
        mPhoneCallPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_PHONE_CALL -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhoneCallPermissionGranted = true
                    callTo(number!!)
                }
            }
        }
    }

}

