package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.api.UserHelper
import com.gz.jey.go4lunch.models.Contact
import com.gz.jey.go4lunch.models.DetailsResult
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.CalculateRatio
import com.gz.jey.go4lunch.utils.SetImageColor
import com.gz.jey.go4lunch.views.DetailsAdapter
import java.text.SimpleDateFormat
import java.util.*

class RestaurantDetailsFragment : Fragment(), DetailsAdapter.Listener{

    private var mView : View? = null

    // FOR DATA
    var mainActivity: MainActivity? = null
    private var metrics : DisplayMetrics? = null

    private var contacts: ArrayList<Contact>? = null
    private var restaurant: DetailsResult? = null
    private var detailsAdapter: DetailsAdapter? = null

    // FOR DESIGN
    private var recyclerView: RecyclerView? = null
    private var restaurantImage : ImageView? = null
    private var restaurantName : TextView? = null
    private var restaurantAddress : TextView? = null
    private var firstStar : ImageView? = null
    private var secondStar : ImageView? = null
    private var thirdStar : ImageView? = null

    private var call : LinearLayout? = null
    private var callTxt : TextView? = null
    private var callImg : ImageView? = null
    private var like : LinearLayout? = null
    private var likeTxt : TextView? = null
    private var likeImg : ImageView? = null
    private var website : LinearLayout? = null
    private var websiteTxt : TextView? = null
    private var websiteImg : ImageView? = null

    private var selectRestaurant : ImageView? = null

    /**
     * CALLED ON INSTANCE OF THIS FRAGMENT TO CREATE VIEW
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.details_fragment, container, false)
        recyclerView = mView!!.findViewById(R.id.recycler_view)

        metrics = DisplayMetrics()
        mainActivity!!.windowManager.defaultDisplay.getMetrics(metrics)
        val goButton = mView!!.findViewById<FrameLayout>(R.id.go_button)
        val buttonSize = (metrics!!.widthPixels * 0.15f).toInt()
        val layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
        val margeTop = mView!!.findViewById<ImageView>(R.id.restaurant_image).layoutParams.height - (buttonSize/2)
        val margeLeft = (metrics!!.widthPixels * 0.8f).toInt()
        layoutParams.setMargins(margeLeft, margeTop, 0, 0 )
        goButton!!.layoutParams = layoutParams

        return mView
    }

    /**
     * CALLED WHEN VIEW CREATED
     * @param view View
     * @param savedInstanceState Bundle
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        setRecyclerView()
        setDetails()
    }

    /**
     * SET THE VIEWS
     * @param view View
     */
    private fun setViews(view : View){
        restaurantImage = view.findViewById(R.id.restaurant_image)
        restaurantName = view.findViewById(R.id.restaurant_name)
        restaurantAddress = view.findViewById(R.id.restaurant_address)
        firstStar = view.findViewById(R.id.first_star)
        secondStar = view.findViewById(R.id.second_star)
        thirdStar = view.findViewById(R.id.third_star)
 
        call = view.findViewById(R.id.call)
        callTxt = view.findViewById(R.id.call_txt)
        callImg = view.findViewById(R.id.call_img)
        like = view.findViewById(R.id.like)
        likeTxt = view.findViewById(R.id.like_txt)
        likeImg = view.findViewById(R.id.like_img)
        website = view.findViewById(R.id.website)
        websiteTxt = view.findViewById(R.id.website_txt)
        websiteImg = view.findViewById(R.id.website_img)

        selectRestaurant = view.findViewById(R.id.select_restaurant)
    }

    /**
     * SET THE RECYCLER VIEW
     */
    private fun setRecyclerView() {
        contacts = ArrayList()
        // Create newsAdapter passing in the sample user data
        detailsAdapter =
                DetailsAdapter(contacts!!,
                        Glide.with(this),
                        this)

        // Attach the detailsAdapter to the recycler-view to populate items
        recyclerView!!.adapter = detailsAdapter
        // Set layout manager to position the items
        recyclerView!!.layoutManager =
                LinearLayoutManager(activity)
    }

    /**
     * SET RESTAURANT DETAILS
     */
    private fun setDetails(){
        restaurant = mainActivity!!.details!!.result

        if(mainActivity!!.user!!.restLiked.contains(restaurant!!.place_id))
            restaurant!!.liked ++

        for (c in mainActivity!!.contacts!!)
            if(c.restLiked.contains(restaurant!!.place_id))
                restaurant!!.liked ++

        if(restaurant!!.photos != null && restaurant!!.photos!!.isNotEmpty()) {
            val imgLink = ApiPhoto.getPhotoURL(500, restaurant!!.photos!![0].photo_reference!!, getString(R.string.google_maps_key))
            Glide.with(this)
                    .load(imgLink)
                    .into(restaurantImage!!)
        }

        restaurantName!!.text = if(restaurant!!.name!!.length>25) restaurant!!.name!!.substring(0,22)+" ..." else restaurant!!.name
        restaurantAddress!!.text = restaurant!!.vicinity

        val emptyStar = SetImageColor.changeDrawableColor(context!!, R.drawable.star_rate, ContextCompat.getColor(context!!, R.color.colorTransparent))
        val googleStar = SetImageColor.changeDrawableColor(context!!, R.drawable.star_rate, ContextCompat.getColor(context!!, R.color.colorPrimary))
        val likeStar = SetImageColor.changeDrawableColor(context!!, R.drawable.star_rate, ContextCompat.getColor(context!!, R.color.colorAccent))

        val xSize = (metrics!!.widthPixels * 0.009f)
        val ySize = (metrics!!.widthPixels * 0.007f)

        firstStar!!.x = xSize
        firstStar!!.y = ySize
        secondStar!!.x = xSize
        secondStar!!.y = ySize
        thirdStar!!.x = xSize
        thirdStar!!.y = ySize

        when(CalculateRatio.getRateOn3(restaurant!!.rating!!)){
            0->{this.firstStar!!.setImageDrawable(emptyStar)
                this.secondStar!!.setImageDrawable(emptyStar)
                this.thirdStar!!.setImageDrawable(emptyStar)
            }
            1 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(emptyStar)
                this.thirdStar!!.setImageDrawable(emptyStar)
            }
            2 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(googleStar)
                this.thirdStar!!.setImageDrawable(emptyStar)
            }
            3 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(googleStar)
                this.thirdStar!!.setImageDrawable(googleStar)
            }
        }

        when(CalculateRatio.getLike(restaurant!!.liked, mainActivity!!.contacts!!.size+1)){
            1 -> {this.firstStar!!.setImageDrawable(likeStar)}
            2 -> {this.firstStar!!.setImageDrawable(likeStar)
                this.secondStar!!.setImageDrawable(likeStar)
            }
            3 -> {this.firstStar!!.setImageDrawable(likeStar)
                this.secondStar!!.setImageDrawable(likeStar)
                this.thirdStar!!.setImageDrawable(likeStar)
            }
        }

        callTxt!!.text = getString(R.string.call)
        var number = restaurant!!.formatted_phone_number
        var open : Boolean? = null
        if(restaurant!!.opening_hours!=null)
            open = if(restaurant!!.opening_hours!!.openNow!=null)
                restaurant!!.opening_hours!!.openNow!!
            else
                false
        if(!number!!.isEmpty() && open!!){
            number = number.replace(" ","")
            call!!.setOnClickListener {
                mainActivity!!.callTo(number)
            }
            callTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorPrimaryDark))
            callImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.call, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))
        }else{
            callTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorGrey))
            callImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.call, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
        }

        likeTxt!!.text = getString(R.string.like)
        var liked = false
        for (l in mainActivity!!.user!!.restLiked){
            if(l==restaurant!!.place_id) {
                liked = true
                break
            }
        }

        setLike(liked)

        websiteTxt!!.text = getString(R.string.website)
        if(restaurant!!.website!= null && restaurant!!.website!!.isNotEmpty()){
            website!!.setOnClickListener {
                mainActivity!!.openWebsite(restaurant!!.website!!)
            }
            websiteTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorPrimaryDark))
            websiteImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.world, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))
        }else{
            websiteTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorGrey))
            websiteImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.world, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
        }

        if(mainActivity!!.user!!.whereEatID == mainActivity!!.restaurantID){
            selectRestaurant!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.check_circle, ContextCompat.getColor(mainActivity!!, R.color.colorAccent)))
            mainActivity!!.user!!.whereEatID = mainActivity!!.restaurantID!!
            selectRestaurant!!.setOnClickListener {
                removeRestaurant()
            }
        }else {
            selectRestaurant!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.check, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
            selectRestaurant!!.setOnClickListener {
                goToRestaurant()
            }
        }

        val workmate : ArrayList<Contact> = ArrayList()

        for (c in mainActivity!!.contacts!!){
            if(c.whereEatID == restaurant!!.place_id){
                Log.d("CONTACT" , c.username)
                workmate.add(c)
            }
        }

        updateUI(workmate)
    }

    /**
     * CALLED WHEN CLICK TO ADD RESTAURANT TO WISH
     */
    private fun goToRestaurant(){
        selectRestaurant!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.check_circle, ContextCompat.getColor(mainActivity!!, R.color.colorAccent)))
        mainActivity!!.user!!.whereEatID = mainActivity!!.restaurantID!!
        mainActivity!!.user!!.whereEatName = mainActivity!!.restaurantName!!
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
        val now : Calendar = Calendar.getInstance()
        val nowF = df.format(now.time)
        mainActivity!!.user!!.whereEatDate = nowF
        UserHelper.updateUser(mainActivity!!.user!!.uid, mainActivity!!.user!!)
        selectRestaurant!!.setOnClickListener {
            removeRestaurant()
        }
    }

    /**
     * CALLED WHEN CLICK TO REMOVE RESTAURANT FROM WISH
     */
    private fun removeRestaurant(){
        selectRestaurant!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.check, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
        mainActivity!!.restaurantID = ""
        mainActivity!!.restaurantName = ""
        mainActivity!!.user!!.whereEatID = mainActivity!!.restaurantID!!
        mainActivity!!.user!!.whereEatName = mainActivity!!.restaurantName!!
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
        val now : Calendar = Calendar.getInstance()
        val nowF = df.format(now.time)
        mainActivity!!.user!!.whereEatDate = nowF
        UserHelper.updateUser(mainActivity!!.user!!.uid, mainActivity!!.user!!)
        selectRestaurant!!.setOnClickListener {
            goToRestaurant()
        }
    }

    /**
     * SET LIKE OR NOT
     * @param liked Boolean
     */
    private fun setLike(liked : Boolean){
        if(liked){
            like!!.setOnClickListener {
                val nliked = false
                mainActivity!!.user!!.restLiked.remove(restaurant!!.place_id)
                UserHelper.updateUser(mainActivity!!.user!!.uid, mainActivity!!.user!!)
                setLike(nliked)
            }
            likeTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorAccent))
            likeImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorAccent)))
        }else{
            like!!.setOnClickListener {
                val nliked = true
                mainActivity!!.user!!.restLiked.add(restaurant!!.place_id!!)
                UserHelper.updateUser(mainActivity!!.user!!.uid, mainActivity!!.user!!)
                setLike(nliked)
            }
            likeTxt!!.setTextColor(ContextCompat.getColor(this.context!!, R.color.colorPrimaryDark))
            likeImg!!.setImageDrawable(SetImageColor.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))
        }
    }



    /**
     * CALLED TO UPDATE WORKMATE LIST
     * @param workmate ArrayList<Contact>
     */
    private fun updateUI(workmate: ArrayList<Contact>) {
        if(contacts!=null)
            contacts!!.clear()
        else
            contacts = ArrayList()

        contacts!!.addAll(workmate)

        if (contacts!!.size != 0) {
            mView!!.findViewById<TextView>(R.id.no_result_text).visibility = View.GONE
            detailsAdapter!!.notifyDataSetChanged()
        }else{
            view!!.findViewById<TextView>(R.id.no_result_text).visibility = View.VISIBLE
            view!!.findViewById<TextView>(R.id.no_result_text).text = getString(R.string.none_joining)
        }

        mainActivity!!.setLoading(false, false)
    }

    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new RestaurantDetailsFragment()
         */
        fun newInstance(mainActivity : MainActivity): RestaurantDetailsFragment {
            val fragment = RestaurantDetailsFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }

}