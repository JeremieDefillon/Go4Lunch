package com.gz.jey.go4lunch

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.gz.jey.go4lunch.activities.MainActivity
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
class AuthenticationTest {

    @get:Rule
    val mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private var mActivity: MainActivity? = null

    /**
     * The setUp
     */
    @Before
    fun setUp() {
        mActivity = mActivityRule.activity
    }
    /**
     * Testing authentication
     */
    @Test
    fun testIfConnected() {
        val isLogged = mActivityRule.activity.isCurrentUserLogged()
        assertEquals(true, isLogged)
    }

    /**
     * to destroy activity
     */
    @After
    fun tearDown() {
        mActivity = null
    }
}
