package com.gz.jey.go4lunch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import java.util.*


class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN : Int = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signInActivity()
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
                RC_SIGN_IN)
    }

    fun getAccountName() : String{
        return "JeyTest"
    }
}
