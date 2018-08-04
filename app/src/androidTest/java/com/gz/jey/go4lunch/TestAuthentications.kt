package com.gz.jey.go4lunch

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestAuthentications {

    @get:Rule
    val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private var mActivity: MainActivity? = null

    /**
     * The SetUp
     */
    @Before
    fun SetUp() {
        mActivity = mActivityRule.getActivity()
    }
    /**
     * Testing authentication with google's account connecting.
     */
    @Test
    fun useConnectWithGoogle() {
        // Proceed log in with google
        val accountName = mActivityRule.activity.getAccountName()
        assertEquals("JeyTest", accountName)
    }

    /**
     * Testing authentication with facebook's account connecting.
     */
    @Test
    fun useConnectWithFacebook() {
        // Proceed log in with facebook
        val accountName = mActivityRule.activity.getAccountName()
        assertEquals("JeyTest", accountName)
    }

    /**
     * to destroy activity
     */
    @After
    fun tearDown() {
        mActivity = null
    }
}
