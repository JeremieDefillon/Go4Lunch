package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Place
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiStreams
import com.gz.jey.go4lunch.utils.ItemClickSupport
import com.gz.jey.go4lunch.views.RestaurantsAdapter
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class RestaurantsFragment : Fragment(), RestaurantsAdapter.Listener{

    private var mView : View? = null

    // FOR DESIGN
    private var recyclerView: RecyclerView? = null

    var mainActivity: MainActivity? = null
    private var disposable: Disposable? = null

    private var results: ArrayList<Result>? = null
    private var restaurantsAdapter: RestaurantsAdapter? = null

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.recycler_view_fragment, container, false)
        mainActivity = activity as MainActivity
        recyclerView = mView!!.findViewById(R.id.recycler_view)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setOnClickRecyclerView()
        executeHttpRequestWithRetrofit("place")
    }

    /**
     * to set the RecyclerView
     */
    private fun setRecyclerView() {
        results = ArrayList()
        // Create newsAdapter passing in the sample user data
        restaurantsAdapter = RestaurantsAdapter(
                        getString(R.string.google_api_key),
                        mainActivity!!.mLastKnownLocation!!,
                        results!!,
                        Glide.with(this),
                        this)

        // Attach the restaurantsAdapter to the recycler-view to populate items
        recyclerView!!.adapter = restaurantsAdapter
        // Set layout manager to position the items
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
    }

    // -----------------
    // ACTION
    // -----------------

    /**
     * to Set the onClick function from items in RecyclerView
     */
    private fun setOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView!!, R.layout.restaurant_item)
                .setOnItemClickListener { _, position, _ ->
                    mainActivity!!.restaurantID = mainActivity!!.place!!.results[position].id
                    mainActivity!!.restaurantName = mainActivity!!.place!!.results[position].name
                    mainActivity!!.setDetailsRestaurant()
                }
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------
    private fun executeHttpRequestWithRetrofit(req : String) {
        when (req) {
            "place" -> {
                val location : LatLng =
                    if(mainActivity!!.mLastKnownLocation == null) mainActivity!!.mDefaultLocation!!
                    else mainActivity!!.mLastKnownLocation!!
                disposable = ApiStreams.streamFetchRestaurants(getString(R.string.google_maps_key), location, mainActivity!!.lang)
                    .subscribeWith(object : DisposableObserver<Place>(){
                    override fun onNext(place: Place) {
                        UpdateUI(place)
                    }

                    override fun onError(e: Throwable) {
                        Log.e("MAP RX", e.toString())
                    }

                    override fun onComplete() {}
                })
            }
        }
    }

    /**
     * @param place Place
     * called while request get back models
     */
    private fun UpdateUI(place: Place) {
        mainActivity!!.place = place
        if (results != null)
            results!!.clear()
        else
            results = ArrayList()

        results!!.addAll(place.results)

        for(c in mainActivity!!.contacts){
            if(!c.whereEatID.isEmpty()){
                for(r in place!!.results){
                    if(r.id==c.whereEatID)
                        if(!r.workmates.contains(c)) {
                            r.workmates.add(c)
                            break
                        }
                }
            }
        }

        if (results!!.size != 0) {
            mView!!.findViewById<TextView>(R.id.no_result_text).visibility = GONE
            restaurantsAdapter!!.notifyDataSetChanged()
        }else{
            view!!.findViewById<TextView>(R.id.no_result_text).visibility = VISIBLE
            view!!.findViewById<TextView>(R.id.no_result_text).text = getString(R.string.none_restaurant)
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
         * @return new RestaurantsFragment()
         */
        fun newInstance(mainActivity : MainActivity): RestaurantsFragment {
            val fragment = RestaurantsFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }
}