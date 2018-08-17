package com.gz.jey.go4lunch.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Place
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiStreams
import com.gz.jey.go4lunch.views.RestaurantsAdapter
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class RestaurantsFragment : Fragment(), RestaurantsAdapter.Listener{

    // FOR DESIGN
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

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
        val view= inflater.inflate(R.layout.recycler_view_fragment, container, false)
        recyclerView = mainActivity!!.findViewById(R.id.recycler_view)
        swipeRefreshLayout = mainActivity!!.findViewById(R.id.swipe_container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setSwipeRefreshLayout()
        executeHttpRequestWithRetrofit("place")
    }

    /**
     * to set the RecyclerView
     */
    private fun setRecyclerView() {
        results = ArrayList()
        // Create newsAdapter passing in the sample user data
        restaurantsAdapter =
                RestaurantsAdapter(getString(R.string.google_api_key), mainActivity!!.mLastKnownLocation!!, results!!, Glide.with(this), this)
        // Attach the restaurantsAdapter to the recyclerview to populate items
        recyclerView!!.adapter =
                restaurantsAdapter
        // Set layout manager to position the items
        recyclerView!!.layoutManager =
                LinearLayoutManager(activity)
    }

    /**
     * to set the SwipeRefreshLayout Listeners
     */
    private fun setSwipeRefreshLayout() {
        swipeRefreshLayout!!.setOnRefreshListener { executeHttpRequestWithRetrofit("place") }
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
        if (results != null)
            results!!.clear()
        else
            results = ArrayList()

        results!!.addAll(place.results)

        if (results!!.size != 0) {
            restaurantsAdapter!!.notifyDataSetChanged()
        }
        swipeRefreshLayout!!.isRefreshing = false
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