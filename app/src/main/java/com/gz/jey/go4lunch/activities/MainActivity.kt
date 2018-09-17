package com.gz.jey.go4lunch.activities

 import android.annotation.SuppressLint
 import android.app.Activity
 import android.app.AlarmManager
 import android.app.PendingIntent
 import android.content.Context
 import android.content.Intent
 import android.content.pm.PackageManager
 import android.net.Uri
 import android.os.Bundle
 import android.speech.RecognitionListener
 import android.speech.RecognizerIntent
 import android.speech.SpeechRecognizer
 import android.support.design.widget.BottomNavigationView
 import android.support.design.widget.NavigationView
 import android.support.design.widget.TextInputEditText
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
 import android.view.MotionEvent
 import android.view.View
 import android.view.View.GONE
 import android.view.View.VISIBLE
 import android.view.inputmethod.InputMethodManager
 import android.widget.*
 import com.bumptech.glide.Glide
 import com.bumptech.glide.request.RequestOptions
 import com.firebase.ui.auth.AuthUI
 import com.firebase.ui.auth.ErrorCodes
 import com.firebase.ui.auth.IdpResponse
 import com.google.android.gms.location.FusedLocationProviderClient
 import com.google.android.gms.location.LocationServices
 import com.google.android.gms.location.places.*
 import com.google.android.gms.maps.model.LatLng
 import com.google.android.gms.maps.model.LatLngBounds
 import com.google.android.gms.tasks.OnFailureListener
 import com.google.android.gms.tasks.OnSuccessListener
 import com.google.firebase.auth.FirebaseAuth
 import com.google.firebase.auth.FirebaseUser
 import com.google.firebase.firestore.DocumentSnapshot
 import com.google.firebase.firestore.FirebaseFirestore
 import com.google.firebase.firestore.FirebaseFirestoreSettings
 import com.google.maps.android.SphericalUtil
 import com.gz.jey.go4lunch.R
 import com.gz.jey.go4lunch.adapters.PlacesAdapter
 import com.gz.jey.go4lunch.api.UserHelper
 import com.gz.jey.go4lunch.fragments.*
 import com.gz.jey.go4lunch.models.*
 import com.gz.jey.go4lunch.models.Place
 import com.gz.jey.go4lunch.utils.ApiStreams
 import com.gz.jey.go4lunch.utils.CheckIfTest
 import com.gz.jey.go4lunch.utils.SetImageColor
 import io.reactivex.disposables.Disposable
 import io.reactivex.observers.DisposableObserver
 import java.text.SimpleDateFormat
 import java.util.*
 import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    // FRAGMENTS
    private val TAG = "MainActivity"
    private var signInFragment: SignInFragment? = null
    private var mapViewFragment: MapViewFragment? = null
    private var restaurantsFragment: RestaurantsFragment? = null
    private var workmatesFragment: WorkmatesFragment? = null
    private var detailsFragment: RestaurantDetailsFragment? = null
    private var settingsFragment: SettingsFragment? = null
    private lateinit var placesAdapter: PlacesAdapter

    // FOR PERMISSIONS
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 34
    var mLocationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_PHONE_CALL = 69
    private var mPhoneCallPermissionGranted = false
    private val PERMISSIONS_MICROPHONE = 75
    private var mMicrophonePermissionGranted = false

    // TASK CODE
    private val SIGN_OUT_TASK = 99
    private val CODE_GPS = 10
    val CODE_RESTAURANTS = 34
    private val CODE_CONTACTS = 37
    val CODE_DETAILS = 44

    // FOR POSITION
    private var mGeoDataClient : GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
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
    private var loadingContent : TextView? = null

    // FOR DATA
    var user : User? = null
    var contacts : ArrayList<Contact>? = null
    var places : ArrayList<Result>? = null
    var details : Details? = null
    private var lastTab = 2
    private var username : String?= null
    var email : String?= null
    private var number : String? = null
    var lang = 1
    private var hiddenItems = false
    private var fromRestaurants : Boolean = false
    private var fromNotif : Boolean = false
    private var fromDetails : Boolean = false
    private var gettingContacts : Boolean = false
    private var gettingRestaurants : Boolean = false
    private var gettingDetails : Boolean = false
    var changedFilter : Boolean = false

    // FOR RESTAURANTS SELECTOR
    var restaurantID: String? = null
    var restaurantName: String? = null

    // FOR REQUEST
    private var disposable : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        loadDatas()
        loading = findViewById(R.id.loading)
        loadingContent = findViewById(R.id.content_loading)
        loadingContent!!.text = getString(R.string.initActivity)
        setLoading(true, true)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        val settings : FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
        .setTimestampsInSnapshotsEnabled(true)
        .build()
        firestore.firestoreSettings = settings

        Log.d("ENABLE NOTIF ???", Data.enableNotif.toString())

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null){
                fromNotif = extras.getBoolean("NotiClick")
                restaurantID = extras.getString("RestaurantId")
                restaurantName = extras.getString("RestaurantName")
            }

            if(Data.enableNotif && !fromNotif)
                setNotification()

            initActivity()
        }
    }

    private fun initActivity(){
        saveDatas()
        if(!CheckIfTest.isRunningTest("NavDrawerTest"))
            when(isCurrentUserLogged()){
                true -> {
                    Log.d("CURRENT USER", "TRUE")
                    checkUserInFirestore()
                    this.setLocalisationData()
                    execRequest(CODE_GPS)
                    this.configureToolBar()
                    this.configureBottomBar()
                    this.setDrawerLayout()

                    saveDatas()
                }
                false -> {
                    Log.d("CURRENT USER", "FALSE")
                    setFragment(0)
                }
                else -> {
                    Log.d("CURRENT USER", "ELSE")
                    setFragment(0)
                }
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

       val searchWorkmate : TextInputEditText = searchBar!!.findViewById(R.id.search_workmate)
       val searchRestaurant : AutoCompleteTextView = searchBar!!.findViewById(R.id.search_restaurant)
       searchWorkmate.visibility = GONE
       searchRestaurant.visibility = GONE
       when(tab){
           3->{
               searchWorkmate.visibility = VISIBLE
               setOnClickSearchWorkmate(searchWorkmate)

               searchWorkmate.hint = getString(R.string.search_workmates)
               searchWorkmate.addTextChangedListener(object : TextWatcher {
                   override fun beforeTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) { }
                   override fun onTextChanged(charSequence : CharSequence, i : Int, i1 : Int, i2 : Int) { }
                   override fun afterTextChanged(editable : Editable) {
                        val contactsFetched : ArrayList<Contact> = ArrayList()
                        contactsFetched.clear()
                        for(c in contacts!!){
                            if(c.username.contains(editable, true)){
                                contactsFetched.add(c)
                            }
                        }
                        workmatesFragment!!.updateUI(contactsFetched)
                   }
               })
           }
           else->{
               searchRestaurant.visibility = VISIBLE
               setOnClickSearchRestaurant(searchRestaurant)
               searchRestaurant.hint = getString(R.string.search_restaurants)

               val typeFilter = AutocompleteFilter.Builder()
                       .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                       .build()

               val bounds = LatLngBounds(LatLng(mLastKnownLocation!!.latitude-0.01, mLastKnownLocation!!.longitude-0.01), LatLng(mLastKnownLocation!!.latitude+0.01, mLastKnownLocation!!.longitude+0.01))

               placesAdapter = PlacesAdapter(this, android.R.layout.simple_list_item_1, mGeoDataClient!!, typeFilter, bounds)
               searchRestaurant.setAdapter(placesAdapter)

               searchRestaurant.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                   // This is your listview's selected item
                   val item = parent.getItemAtPosition(position) as AutocompletePrediction
                   restaurantID = item.placeId
                   execRequest(CODE_DETAILS)
                   toolbar!!.visibility = VISIBLE
                   searchBar!!.visibility = GONE
               }
           }
       }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnClickSearchRestaurant(input : AutoCompleteTextView){
        val search = SetImageColor.changeDrawableColor(this ,R.drawable.search, ContextCompat.getColor(this, R.color.colorGrey))
        val mic= SetImageColor.changeDrawableColor(this ,R.drawable.mic, ContextCompat.getColor(this, R.color.colorPrimary))
        val cross = SetImageColor.changeDrawableColor(this ,R.drawable.close, ContextCompat.getColor(this, R.color.colorBlack))
        input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, mic,null)

        input.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                when {
                    event.rawX >= (input.right - input.compoundDrawables[2].bounds.width()) -> {
                        if (!input.isActivated) {
                            if(mMicrophonePermissionGranted)
                                startSpeechToText(input)
                            else
                                getMicrophonePermission()
                        } else {
                            input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, mic,null)
                            hideKeyboard()
                            input.text = null
                            input.hint = getString(R.string.search_restaurants)
                            input.isActivated = false
                            toolbar!!.visibility = VISIBLE
                            searchBar!!.visibility = GONE
                        }
                    }
                    else -> {
                        input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, cross,null)
                        input.requestFocus()
                        input.isActivated = true
                        displayKeyboard(input)
                    }
                }
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnClickSearchWorkmate(input : TextInputEditText){
        val search = SetImageColor.changeDrawableColor(this ,R.drawable.search, ContextCompat.getColor(this, R.color.colorGrey))
        val mic= SetImageColor.changeDrawableColor(this ,R.drawable.mic, ContextCompat.getColor(this, R.color.colorPrimary))
        val cross = SetImageColor.changeDrawableColor(this ,R.drawable.close, ContextCompat.getColor(this, R.color.colorBlack))
        input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, mic,null)

        input.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                when {
                    event.rawX >= (input.right - input.compoundDrawables[2].bounds.width()) -> {
                        if (!input.isActivated) {
                            if(mMicrophonePermissionGranted)
                                startSpeechToText(input)
                            else
                                getMicrophonePermission()
                        } else {
                            input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, mic,null)
                            hideKeyboard()
                            input.text = null
                            input.hint = getString(R.string.search_workmates)
                            input.isActivated = false
                            toolbar!!.visibility = VISIBLE
                            searchBar!!.visibility = GONE
                        }
                    }
                    else -> {
                        input.setCompoundDrawablesRelativeWithIntrinsicBounds(search,null, cross,null)
                        input.requestFocus()
                        input.isActivated = true
                        displayKeyboard(input)
                    }
                }
            }
            true
        }
    }

    private fun startSpeechToText(input: EditText) {
        val mic= SetImageColor.changeDrawableColor(this ,R.drawable.mic, ContextCompat.getColor(this, R.color.colorAccent))
        val cross = SetImageColor.changeDrawableColor(this ,R.drawable.close, ContextCompat.getColor(this, R.color.colorBlack))
        input.setCompoundDrawablesRelativeWithIntrinsicBounds(mic,null, cross,null)
        input.requestFocus()
        input.isActivated = true

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {speechRecognizer.stopListening()}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)//getting all the matches
                //displaying the first match
                if (matches != null)
                    input.setText(matches[0])
            }

            override fun onPartialResults(bundle: Bundle) {}

            override fun onEvent(i: Int, bundle: Bundle) {}
        })

        speechRecognizer.startListening(speechRecognizerIntent)
        input.setText("")
    }

    /**
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolMenu = menu
        // Inflate the menu and add it to the Toolbar
        menuInflater.inflate(R.menu.menu_toolbar, toolMenu)
        if (hiddenItems)
            for (i in 0 until menu.size())
                menu.getItem(i).isVisible = false
        else
            for (i in 0 until menu.size())
                menu.getItem(i).isVisible = true
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
        this.bottom!!.selectedItemId = when(Data.tab) {
            1 -> R.id.map_button
            2 -> R.id.restaurants_button
            3 -> R.id.workmates_button
            else -> R.id.restaurants_button
        }
        this.bottom!!.setOnNavigationItemSelectedListener { item ->
            setLoading(false, true)
            when (item.itemId) {
                R.id.map_button -> {
                    Data.tab = 1
                    execRequest(CODE_GPS)}
                R.id.restaurants_button -> {
                    Data.tab = 2
                    execRequest(CODE_RESTAURANTS)
                }
                R.id.workmates_button -> {
                    Data.tab = 3
                    execRequest(CODE_CONTACTS)
                }
            }
            saveDatas()
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

        //Get picture URL from Firebase
        Glide.with(this)
            .load(user!!.urlPicture)
            .apply(RequestOptions.circleCropTransform())
            .into(accountPicture!!)


        //Get email & username from Firebase
        email = if (TextUtils.isEmpty(user!!.mail))
            getString(R.string.no_email_found) else user!!.mail
        username = if (TextUtils.isEmpty(user!!.username))
            getString(R.string.no_username_found) else user!!.username

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
                if(user!!.whereEatID.isNotEmpty()){
                    restaurantID = user!!.whereEatID
                    restaurantName = user!!.whereEatName
                    execRequest(CODE_DETAILS)
                }else{
                    popupMsg(getString(R.string.none_restaurant))
                }
            }
            R.id.settings -> setFragment(5)
            R.id.power_settings -> disconnect()
        }
        this.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }


    /**
     * @param index Int
     * Change Fragment
     */
    private fun setFragment(index: Int){
        loadingContent!!.text = getString(R.string.loadingView)
        hideKeyboard()
        if (fromNotif) {
            fromNotif=false
            execRequest(CODE_DETAILS)
        }else {
            var fragment: Fragment? = null
            invalidateOptionsMenu()
            Data.tab = index
            when (index) {
                0 -> {
                    this.signInFragment = SignInFragment.newInstance(this)
                    fragment = this.signInFragment
                }
                1 -> {
                    lastTab = index
                    this.mapViewFragment = MapViewFragment.newInstance(this)
                    fragment = this.mapViewFragment
                }
                2 -> {
                    lastTab = index
                    this.restaurantsFragment = RestaurantsFragment.newInstance(this)
                    fragment = this.restaurantsFragment
                }
                3 -> {
                    lastTab = index
                    this.workmatesFragment = WorkmatesFragment.newInstance(this)
                    fragment = this.workmatesFragment
                }
                4 -> {
                    setSpecificFrame()
                    this.detailsFragment = RestaurantDetailsFragment.newInstance(this)
                    fragment = this.detailsFragment
                }
                5 -> {
                    setSpecificFrame()
                    this.settingsFragment = SettingsFragment.newInstance(this)
                    fragment = this.settingsFragment
                }
            }

            if (index != 0 && index != 4 && index !=5) {
                this.configureSearchBar(lastTab)
                Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.menu)
                setDrawerLayout()
                hiddenItems = false
                bottom!!.visibility = VISIBLE
                setFrameLayoutMargin(true)
            }

            this.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
        }
    }

    private fun setSpecificFrame(){
        Data.tab = lastTab
        Objects.requireNonNull<ActionBar>(supportActionBar).setHomeAsUpIndicator(R.drawable.back_button)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        hiddenItems = true
        toolbar!!.setNavigationOnClickListener {
            setLoading(true, true)
            when (lastTab) {
                1 -> execRequest(CODE_GPS)
                2 -> execRequest(CODE_RESTAURANTS)
                3 -> execRequest(CODE_CONTACTS)
                else -> setFragment(lastTab)
            }
        }
        bottom!!.visibility = GONE
        setFrameLayoutMargin(false)
    }

    fun popupMsg(msg : String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * the calling notifcation with alarmManager (once per day)
     */
    fun setNotification() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, Data.notifHour)
        cal.set(Calendar.MINUTE, Data.notifMinute)
        cal.set(Calendar.SECOND, 0)

        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext,
                987, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        Log.d("NOTIF TIME", cal.time.toString())
    }

    /**
     * to disable notification
     */
    fun cancelNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, 987, myIntent, 0)

        assert(true)
        alarmManager.cancel(pendingIntent)
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
                    loadUserData(it)
                else
                    createUserInFirestore()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    private fun loadUserData(data : DocumentSnapshot){
        loadingContent!!.text = getString(R.string.checkingWorkmates)
        user=User(
                data["uid"] as String,
                data["username"] as String,
                data["mail"] as String,
                data["urlPicture"] as String,
                data["whereEatID"] as String,
                data["whereEatName"] as String,
                data["whereEatDate"] as String,
                data["restLiked"] as ArrayList<String> )

        this.setNavigationView()

        contacts = ArrayList()
        contacts!!.clear()
        UserHelper.getUsersCollection().get().addOnSuccessListener {
            for(contact in it.documents){
                if(contact.get("uid").toString() != getCurrentUser()!!.uid) {
                    val username = contact.get("username").toString()
                    val urlPicture = contact.get("urlPicture").toString()
                    val whereEatID= contact.get("whereEatID").toString()
                    val whereEatName= contact.get("whereEatName").toString()
                    val whereEatDate= if(contact.get("whereEatDate").toString().isEmpty()) "0000-00-00 00:00:00" else contact.get("whereEatDate").toString()
                    val restLiked= contact.get("restLiked") as ArrayList<String>


                    // WAS TO RANDOM LIKE RESTAURANTS AROUND FOR EACH CONTACT

                    /*restLiked = ArrayList()
                    val restarr : ArrayList<String> = arrayListOf(
                            "ChIJJypu3ReF9EcRjy51n7iQntI",
                            "ChIJdxbaUiKF9EcRASqCd2nR2fY",
                            "ChIJjaz5ZweB9EcRPB08GrWO86A",
                            "ChIJc_cbmVCZ9EcRcXmJrcgkqCA",
                            "ChIJK07F2TyF9EcRSp1rXbjo1rQ",
                            "ChIJySFR6t-E9EcRFY4mluDzDh8",
                            "ChIJd8eqj9yP9EcRFBLkcqv0gcc",
                            "ChIJwR1AZD2F9EcRqPkUqvtgjWA",
                            "ChIJsc7XhjyF9EcRXXgd12G3ilY",
                            "ChIJ___PN9aE9EcRDmXrj9pDUT4",
                            "ChIJOU3chNeP9EcR31Wpvvvw_hk",
                            "ChIJ5foaYSKF9EcRCu6X8JaFqHs",
                            "ChIJE68UGyab9EcREFJiAZkv0Tk",
                            "ChIJp7JQEyKF9EcR72wJu3P1fUw",
                            "ChIJS5_cVzyF9EcRvDR_5krS0tY",
                            "ChIJU8k9tjyF9EcRYxmfpRjhJ58",
                            "ChIJ8V0s7C2d9EcRgmxKUU5Azt4",
                            "ChIJteWKuGKF9EcRHgkivkIkmNs",
                            "ChIJjTRFnjGF9EcR9u_DyAiBC7M",
                            "ChIJscITJCKF9EcRYO2zkZMXEvE")


                    val max = (10 until restarr.size).random()

                    for (i in 0 until max){
                        val rand = (0 until restarr.size).random()
                        if(!restLiked.contains(restarr[rand]))
                            restLiked.add(restarr[rand])
                    }*/

                    val cntc = Contact(username, urlPicture, whereEatID, whereEatName, whereEatDate, restLiked)

                    //UserHelper.updateContact(uid, cntc)
                    contacts!!.add(cntc)
                    loadingContent!!.text = "${getString(R.string.updatingWorkmates)} ${contacts!!.size}"
                }
            }

            setAllContacts()
        }
    }

    //fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) +  start

    // Http request that create user in firestore
    private fun createUserInFirestore(){
        if (this.getCurrentUser() != null){
            val mail = getCurrentUser()!!.email
            val urlPicture = if(getCurrentUser()!!.photoUrl != null) getCurrentUser()!!.photoUrl.toString() else ""
            val username = getCurrentUser()!!.displayName
            val uid = getCurrentUser()!!.uid
            val whereEat = ""
            val whereDate = ""
            val restLiked : ArrayList<String> = ArrayList()
            UserHelper.createUser(uid, username!!, mail!!, urlPicture, whereEat, whereEat, whereDate, restLiked).addOnFailureListener(this.onFailureListener())
            checkUserInFirestore()
        }else{
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
                Data.tab = lastTab
                initActivity()
                popupMsg(getString(R.string.connection_succeed))
            } else { // ERRORS
                when {
                    response == null -> popupMsg(getString(R.string.error_authentication_canceled))
                    response.error?.equals(ErrorCodes.NO_NETWORK) ?: (false) -> popupMsg(getString(R.string.error_no_internet))
                    response.error?.equals(ErrorCodes.UNKNOWN_ERROR) ?: (false) -> popupMsg(getString(R.string.error_unknown_error))
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
            CODE_GPS ->{
                getDeviceLocation()
            }
            CODE_RESTAURANTS -> {
                if (mLastKnownLocation != null){
                    if(!gettingRestaurants) {
                        gettingRestaurants = true
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
                }else{
                    fromRestaurants = true
                    getDeviceLocation()
                }
            }
            CODE_CONTACTS -> {
                if(!gettingContacts) {
                    gettingContacts = true
                    checkUserInFirestore()
                }
            }
            CODE_DETAILS -> {
                if(mLastKnownLocation != null) {
                    if(!gettingDetails) {
                        gettingDetails = true
                        disposable = ApiStreams.streamFetchDetails(getString(R.string.google_maps_key), restaurantID!!, lang)
                                .subscribeWith(object : DisposableObserver<Details>() {
                                    override fun onNext(details: Details) {
                                        setDetailsObject(details)
                                    }

                                    override fun onError(e: Throwable) {
                                        Log.e("CODE_DETAILS RX", e.toString())
                                    }

                                    override fun onComplete() {}
                                })
                    }
                }else{
                    fromDetails=true
                    getDeviceLocation()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setAllRestaurants(p : Place){
        loadingContent!!.text = getString(R.string.checkingRestaurants)
        places = p.results as ArrayList<Result>

        for ((index, r) in places!!.withIndex()) {
            r.distance = SphericalUtil.computeDistanceBetween(mLastKnownLocation, LatLng(r.geometry!!.location!!.lat!!, r.geometry.location!!.lng!!))
            loadingContent!!.text = "${getString(R.string.updatingRestaurants)} $index"
        }
        gettingRestaurants=false
        if(contacts != null)
            setAllContacts()
        else{
            if(!gettingContacts)
                execRequest(CODE_CONTACTS)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAllContacts(){
        loadingContent!!.text = getString(R.string.checkingDatas)
        if(places!=null){
            for((i,r) in places!!.withIndex()){
                if(user!!.restLiked.contains(r.place_id))
                    r.liked++
                for((y, c )in contacts!!.withIndex()){
                    loadingContent!!.text = "${getString(R.string.updatingDatas)} ${(i + 1) * (y + 1)}"
                    if(c.restLiked.contains(r.place_id))
                        r.liked++
                }
            }

            val sortedRestaurant = when(Data.filter){
                1 -> places!!.sortedWith(compareBy { it.distance })
                2 -> places!!.sortedWith(compareByDescending { it.liked })
                3 -> places!!.sortedWith(compareByDescending { it.rating })
                4 -> places!!.sortedWith(compareByDescending { it.distance })
                5 -> places!!.sortedWith(compareBy { it.liked })
                6 -> places!!.sortedWith(compareBy { it.rating })
                else -> places!!.sortedWith(compareBy { it.distance })
            }

            places!!.clear()
            places!!.addAll(sortedRestaurant)


            //for ((index, r) in place!!.results.withIndex())
            //    Log.d(index.toString(), r.distance.toString() + " " + r.liked.toString() + " " + r.rating.toString())


            val today : Calendar = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)

            for(c in contacts!!){
                val now : Calendar = Calendar.getInstance()
                now.time = df.parse(c.whereEatDate)

                val nowTime = df.format(now.time)

                if(c.whereEatID.isNotEmpty() && now.after(today)){
                    for(r in places!!){
                        if(r.place_id==c.whereEatID) {
                            if (!r.workmates.contains(c)) {
                                r.workmates.add(c)
                                break
                            }
                        }
                    }
                }else{
                    c.whereEatDate = nowTime
                    c.whereEatID = ""
                    c.whereEatName = ""
                    // UNCOMMENT UNDER IF WANNA SETUP FIRESTORE BUT UPDATE AUTORIZATION WITH
                    //UserHelper.updateContact(c.uid, c)
                }
            }

            gettingContacts=false
            if(changedFilter){
                changedFilter = false
                setFragment(lastTab)
            }else
                setFragment(Data.tab)
        }else{
            if(!gettingRestaurants)
                execRequest(CODE_RESTAURANTS)
        }
    }

    fun setDetailsObject(det: Details){
        gettingDetails=false
        this.details = det

        val fType = details!!.result.types

        if(fType.contains("meal_takeaway") || fType.contains("restaurant")) {
            Data.tab = 4
            saveDatas()
            setFragment(4)
        }else {
            popupMsg("This is NOT a restaurant !")
        }
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        mFusedLocationProviderClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    Log.d(TAG, " TASK DEVICE LOCATION SUCCESS")
                    mLastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                    when {
                        fromDetails -> {
                            fromDetails=false
                            execRequest(CODE_DETAILS)
                        }
                        fromRestaurants -> {
                            fromRestaurants=false
                            execRequest(CODE_RESTAURANTS)
                        }
                        else -> execRequest(CODE_RESTAURANTS)
                    }
                } else {
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

    private fun setFrameLayoutMargin(gotMarge : Boolean){
        val marge = calculateActionBar()
        val bottom = if(gotMarge) marge else 0
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

    private fun getMicrophonePermission(){

        if (ContextCompat.checkSelfPermission(applicationContext,
                        android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            mMicrophonePermissionGranted = true
            //micro(number!!)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    PERMISSIONS_MICROPHONE)
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
        mMicrophonePermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                    getDeviceLocation()
                }
            }
            PERMISSIONS_REQUEST_PHONE_CALL -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhoneCallPermissionGranted = true
                    callTo(number!!)
                }
            }
            PERMISSIONS_MICROPHONE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMicrophonePermissionGranted = true
                    //micro(number!!)
                }
            }
        }
    }

    fun setLoading(type : Boolean, onoff : Boolean){
        val fllp : DrawerLayout.LayoutParams = loading!!.layoutParams as DrawerLayout.LayoutParams
        if(onoff){
            if(type){
                fllp.setMargins(0,0,0,0)
                loading!!.background = ContextCompat.getDrawable(this, R.drawable.login_screen)
                loading!!.findViewById<TextView>(R.id.title_loading).setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.label_loading).setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }else{
                val size = calculateActionBar()
                fllp.setMargins(0,size,0,size)
                loading!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.title_loading).setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                loading!!.findViewById<TextView>(R.id.label_loading).setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            }
            loading!!.visibility = VISIBLE
        }
        else
            loading!!.visibility = GONE
    }

    /**
     * load all the saved datas from Preferences
     */
    private fun loadDatas() {
        Data.lang = getPreferences(Context.MODE_PRIVATE).getInt("LANG", 0)
        Data.tab = getPreferences(Context.MODE_PRIVATE).getInt("TAB", 2)
        Data.tab = if(Data.tab!=1 && Data.tab !=2 && Data.tab!=3) 2 else Data.tab
        Data.filter = getPreferences(Context.MODE_PRIVATE).getInt("FILTER", 1)
        Data.enableNotif = getPreferences(Context.MODE_PRIVATE).getBoolean("NOTIF", true)
    }

    /**
     * Save all the datas into Preferences
     */
    fun saveDatas() {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("LANG", Data.lang)
        editor.putInt("TAB", Data.tab)
        editor.putInt("FILTER", Data.filter)
        editor.putBoolean("NOTIF", Data.enableNotif)
        editor.apply()
    }

    private fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayKeyboard(input : View) {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_FORCED)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}


