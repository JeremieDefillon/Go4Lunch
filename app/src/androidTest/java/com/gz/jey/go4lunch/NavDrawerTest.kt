package com.gz.jey.go4lunch

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import com.gz.jey.go4lunch.activities.MainActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Initialize Main Activity
 */
@RunWith(AndroidJUnit4::class)
class NavDrawerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    private var mActivity: MainActivity? = null
    private var mDrawerLayout: DrawerLayout? = null

    /**
     * The setUp
     */
    @Before
    fun setUp() {
        mActivity = mActivityRule.activity
        mDrawerLayout = mActivity!!.findViewById(R.id.activity_main_drawer_layout)
    }

    /**
     * to Test if drawer is closed
     */
    @Test
    fun navDrawerStartIsClosed() {
        // Left Drawer should be closed.
        Assert.assertFalse("Drawer Closed", mDrawerLayout!!.isDrawerOpen(GravityCompat.START))
    }

    /**
     * @throws InterruptedException
     * to Test if drawer is opened after have force click to open it
     */
    @Test
    @Throws(InterruptedException::class)
    fun navDrawerStartIsOpened() {
        // Open Drawer & check if is open.
        mActivity = mActivityRule.activity
        mActivity?.runOnUiThread { mDrawerLayout!!.openDrawer(GravityCompat.START) }
        Thread.sleep(2000)
        Assert.assertTrue("Drawer Opened", mDrawerLayout!!.isDrawerOpen(GravityCompat.START))
    }
}