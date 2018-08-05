package com.gz.jey.go4lunch.activities

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.fragments.MapViewFragment
import com.gz.jey.go4lunch.fragments.SignInFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser



class MainActivity : AppCompatActivity() {

    var signInFragment: SignInFragment? = null
    var mapViewFragment: MapViewFragment? = null


    // FOR DESIGN
    var coordinatorLayout: CoordinatorLayout? = null


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        this.setSignInFragment()
    }

    /**
     * Set Sign In
     */
    private fun setSignInFragment(){
        this.signInFragment = SignInFragment.newInstance(this)
        this.moveFragment(this.signInFragment!!)
    }

    /**
     * Set MapView
     */
    internal fun setMapViewFragment(){
        this.mapViewFragment = MapViewFragment.newInstance(this)
        this.moveFragment(this.mapViewFragment!!)
    }

    /**
     * @param fragment Fragment
     * Add or Change Fragment
     */
    private fun moveFragment(fragment: Fragment){
        this.supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * @param coordinatorLayout CoordinatorLayout
     * @param message String
     */
    internal fun showSnackBar(message: String) {
        if(message == getString(R.string.connection_succeed)){
            setMapViewFragment()
        }
        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout)
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_SHORT).show()
    }

    @Nullable
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

}

