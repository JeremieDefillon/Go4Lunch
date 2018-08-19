package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.models.Workmates
import com.gz.jey.go4lunch.models.WorkmatesList
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.CalculateRate
import com.gz.jey.go4lunch.utils.SetBottomMenuTab
import com.gz.jey.go4lunch.views.DetailsAdapter
import com.gz.jey.go4lunch.views.GlideApp
import io.reactivex.disposables.Disposable
import java.util.*

class RestaurantDetailsFragment : Fragment(), DetailsAdapter.Listener{

    private var mView : View? = null

    var mainActivity: MainActivity? = null
    private var disposable: Disposable? = null

    private var results: ArrayList<Workmates>? = null
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
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.details_fragment, container, false)
        recyclerView = mView!!.findViewById(R.id.workmates_recycler_view)

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        setRecyclerView()
        setDetails()
        executeHttpRequestWithRetrofit("place")
    }

    /**
     * to set the views
     */
    private fun setViews(view : View){
        recyclerView= view.findViewById<RecyclerView>(R.id.workmates_recycler_view)
        restaurantImage = view.findViewById<ImageView>(R.id.restaurant_image)
        restaurantName = view.findViewById<TextView>(R.id.restaurant_name)
        restaurantAddress = view.findViewById<TextView>(R.id.restaurant_address)
        firstStar = view.findViewById<ImageView>(R.id.first_star)
        secondStar = view.findViewById<ImageView>(R.id.second_star)
        thirdStar = view.findViewById<ImageView>(R.id.third_star)

        call = view.findViewById<LinearLayout>(R.id.call)
        callTxt = view.findViewById<TextView>(R.id.call_txt)
        callImg = view.findViewById<ImageView>(R.id.call_img)
        like = view.findViewById<LinearLayout>(R.id.like)
        likeTxt = view.findViewById<TextView>(R.id.like_txt)
        likeImg = view.findViewById<ImageView>(R.id.like_img)
        website = view.findViewById<LinearLayout>(R.id.website)
        websiteTxt = view.findViewById<TextView>(R.id.website_txt)
        websiteImg = view.findViewById<ImageView>(R.id.website_img)

        selectRestaurant = view.findViewById<ImageView>(R.id.select_restaurant)
    }

    /**
     * to set the RecyclerView
     */
    private fun setRecyclerView() {
        results = ArrayList()
        // Create newsAdapter passing in the sample user data
        detailsAdapter =
                DetailsAdapter(getString(R.string.google_api_key),
                        mainActivity!!.mLastKnownLocation!!,
                        results!!,
                        Glide.with(this),
                        this)

        // Attach the detailsAdapter to the recycler-view to populate items
        recyclerView!!.adapter = detailsAdapter
        // Set layout manager to position the items
        recyclerView!!.layoutManager =
                LinearLayoutManager(activity)
    }

    /**
     * to set the details
     */
    private fun setDetails(){

        val restaurant : Result = mainActivity!!.restaurantDetails!!

        val imgLink = ApiPhoto.getPhotoURL(500, restaurant.photos[0].photoReference, getString(R.string.google_maps_key))
        GlideApp.with(this)
                .load(imgLink)
                .into(restaurantImage!!)

        restaurantName!!.text = restaurant.name
        restaurantAddress!!.text = restaurant.formattedAddress

        when(CalculateRate.getRateOn3(restaurant.rating)){
            1 -> {this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
            }
            2 -> {this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
                this.secondStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.secondStar!!.visibility = View.VISIBLE
            }
            3 -> {this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
                this.secondStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.secondStar!!.visibility = View.VISIBLE
                this.thirdStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimary)))
                this.thirdStar!!.visibility = View.VISIBLE
            }
        }

        call!!.setOnClickListener(View.OnClickListener {
            // DO CALL
        })
        callTxt!!.text = getString(R.string.call)
        callImg!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.call, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))

        like!!.setOnClickListener(View.OnClickListener {
            // DO LIKE
        })
        likeTxt!!.text = getString(R.string.like)
        likeImg!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.star_rate, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))

        website!!.setOnClickListener(View.OnClickListener {
            // GO WEBSITE
        })
        websiteTxt!!.text = getString(R.string.website)
        websiteImg!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.world, ContextCompat.getColor(mainActivity!!, R.color.colorPrimaryDark)))

        selectRestaurant!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.check, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
        selectRestaurant!!.setOnClickListener(View.OnClickListener {
            goToRestaurant()
        })
    }

    private fun goToRestaurant(){
        selectRestaurant!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.check_circle, ContextCompat.getColor(mainActivity!!, R.color.colorAccent)))
        selectRestaurant!!.setOnClickListener(View.OnClickListener {
            removeRestaurant()
        })
    }

    private fun removeRestaurant(){
        selectRestaurant!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(mainActivity!!, R.drawable.check, ContextCompat.getColor(mainActivity!!, R.color.colorGrey)))
        selectRestaurant!!.setOnClickListener(View.OnClickListener {
            goToRestaurant()
        })
    }
    // -------------------
    // HTTP (RxJAVA)
    // -------------------
    private fun executeHttpRequestWithRetrofit(req : String) {
        when (req) {
            "place" -> {
                /*val location : LatLng =
                        if(mainActivity!!.mLastKnownLocation == null) mainActivity!!.mDefaultLocation!!
                        else mainActivity!!.mLastKnownLocation!!
                disposable = ApiStreams.streamFetchWorkmates(getString(R.string.google_maps_key), location, mainActivity!!.lang)
                        .subscribeWith(object : DisposableObserver<Place>(){
                            override fun onNext(place: Place) {
                                UpdateUI(place)
                            }

                            override fun onError(e: Throwable) {
                                Log.e("MAP RX", e.toString())
                            }

                            override fun onComplete() {}
                        })*/
            }
        }
    }

    /**
     * @param place Place
     * called while request get back models
     */
    private fun UpdateUI(workmate: WorkmatesList) {
        if (results != null)
            results!!.clear()
        else
            results = ArrayList()

        results!!.addAll(workmate.workmates)

        if (results!!.size != 0) {
            mView!!.findViewById<TextView>(R.id.no_result_text).visibility = View.GONE
            detailsAdapter!!.notifyDataSetChanged()
        }else{
            view!!.findViewById<TextView>(R.id.no_result_text).visibility = View.VISIBLE
        }
    }

    /**
     * to Destroy fragment
     */
    override fun onDestroy() {
        super.onDestroy()
        disposeWhenDestroy()
    }

    /**
     * to destroy disposable and avoid memory leaks
     */
    private fun disposeWhenDestroy() {
        if (disposable != null && !disposable!!.isDisposed)
            disposable!!.dispose()
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