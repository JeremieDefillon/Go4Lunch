package com.gz.jey.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Switch
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Data

class SettingsFragment : Fragment() {

    private var mView: View? = null

    var mainActivity: MainActivity? = null

    private var enableNotifSwitch: Switch? = null
    private var filterChoice: RadioGroup? = null

    /**
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.settings, container, false)
        mainActivity = activity as MainActivity
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
    }

    private fun setOnClicks(){
        enableNotifSwitch = mView!!.findViewById(R.id.switch_enable_notification)
        enableNotifSwitch!!.isChecked = Data.enableNotif
        filterChoice = mView!!.findViewById(R.id.filter_choice)

        when(Data.filter){
            1 -> filterChoice!!.check(R.id.filter_1)
            2 -> filterChoice!!.check(R.id.filter_2)
            3 -> filterChoice!!.check(R.id.filter_3)
            4 -> filterChoice!!.check(R.id.filter_4)
            5 -> filterChoice!!.check(R.id.filter_5)
            6 -> filterChoice!!.check(R.id.filter_6)
            else -> filterChoice!!.check(R.id.filter_1)
        }

        enableNotifSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            Data.enableNotif = isChecked
            if(Data.enableNotif)
                mainActivity!!.setNotification()
            else
                mainActivity!!.cancelNotification()

            mainActivity!!.saveDatas()
        }

        filterChoice!!.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.filter_1 -> Data.filter = 1
                R.id.filter_2 -> Data.filter = 2
                R.id.filter_3 -> Data.filter = 3
                R.id.filter_4 -> Data.filter = 4
                R.id.filter_5 -> Data.filter = 5
                R.id.filter_6 -> Data.filter = 6
            }
            mainActivity!!.changedFilter = true
            mainActivity!!.saveDatas()
            mainActivity!!.execRequest(mainActivity!!.CODE_RESTAURANTS)
        }
    }

    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new com.gz.jey.go4lunch.fragments.SettingsFragment()
         */
        fun newInstance(mainActivity : MainActivity): SettingsFragment {
            val fragment = SettingsFragment()
            fragment.mainActivity = mainActivity
            return fragment
        }
    }
}