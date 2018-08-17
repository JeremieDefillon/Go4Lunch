package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity

class RestaurantsFragment : Fragment(){

    var mainActivity: MainActivity? = null

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view_fragment, container, false)
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