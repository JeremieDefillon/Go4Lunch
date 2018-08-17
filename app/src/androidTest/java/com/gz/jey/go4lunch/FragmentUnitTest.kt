package com.gz.jey.go4lunch


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.app.Fragment
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.fragments.MapViewFragment
import com.gz.jey.go4lunch.fragments.SignInFragment
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class FragmentUnitTest {

    @get:Rule
    val mActivityRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    private var mActivity: MainActivity? = null
    private var flContainer: RelativeLayout? = null

    /**
     * The setUp
     */
    @Before
    fun SetUp() {
        mActivity = mActivityRule.activity
        flContainer = mActivity!!.findViewById(R.id.fragmentContainer)
    }

    /**
     * to Test if container isn't null
     */
    @Test
    fun testContainer() {
        assertNotNull(flContainer)
    }

    /**
     * to Test if SignInFragment does exist once instancied
     */
    @Test
    fun testSignInFragment() {
        // Test if the Main fragment is launched or not
        val fragment = SignInFragment.newInstance(this.mActivity!!)
        testFragment(fragment, flContainer!!, R.id.signIn)
    }

    /**
     * to Test if MapViewFragment does exist once instancied
     */
    @Test
    fun testMapViewFragment() {
        // Test if the Main fragment is launched or not
        val fragment = MapViewFragment.newInstance(this.mActivity!!)
        testFragment(fragment, flContainer!!, R.id.map)
    }




    /**
     * @param fragment Fragment
     * @param container View
     * @param Gen int
     * Verify if the fragment view is in the expected container
     */
    private fun testFragment(fragment: Fragment, container: View, Gen: Int) {
        mActivity!!.supportFragmentManager.beginTransaction().add(container.id, fragment).commitAllowingStateLoss()
        getInstrumentation().waitForIdleSync()
        val view = Objects.requireNonNull<View>(fragment.view).findViewById<View>(Gen)
        assertNotNull(view)
    }

    /**
     * to destroy activity
     */
    @After
    fun tearDown() {
        mActivity = null
    }

}