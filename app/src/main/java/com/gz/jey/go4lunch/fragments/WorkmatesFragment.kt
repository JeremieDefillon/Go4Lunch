package com.gz.jey.go4lunch.fragments

import android.support.v4.app.Fragment
import com.gz.jey.go4lunch.activities.MainActivity

class WorkmatesFragment : Fragment(){


    var mainActivity: MainActivity? = null
    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new WorkmatesFragment()
         */
        fun newInstance(mainActivity : MainActivity): WorkmatesFragment {
            val fragment = WorkmatesFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }
}