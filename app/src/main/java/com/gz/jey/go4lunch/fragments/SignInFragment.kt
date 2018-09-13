package com.gz.jey.go4lunch.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.activities.MainActivity
import java.util.*

class SignInFragment : Fragment() {

    var mainActivity: MainActivity? = null
    val rcSignIn : Int = 123

    companion object {
        /**
         * @param mainActivity MainActivity
         * @return new SignInFragment()
         */
        fun newInstance(mainActivity : MainActivity): SignInFragment {
            val fragment = SignInFragment()
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
        val view = inflater.inflate(R.layout.sign_in, container, false)
        signInActivity()
        return view
    }

    private fun signInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(AuthUI.IdpConfig.GoogleBuilder().build(),
                                        AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.big_logo)

                        .build(),
                rcSignIn)
    }

    /**
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        // 4 - Handle SignIn Activity response on activity result
        mainActivity!!.handleResponseAfterSignIn(requestCode, resultCode, data)
    }

}


