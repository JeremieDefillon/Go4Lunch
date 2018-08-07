package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity

class MapViewFragment : Fragment() {

    var mainActivity: MainActivity? = null


    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new SignInFragment()
         */
        fun newInstance(mainActivity : MainActivity): MapViewFragment {
            val fragment = MapViewFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_view, container, false)
        mapActivity()
        return view
    }

    private fun mapActivity(){
        mainActivity?.onTabSelected(0)
    }


    /**
     * Destroy this Fragment
     */
    override fun onDestroy() {
        super.onDestroy()
    }
}