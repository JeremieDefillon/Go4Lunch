package com.gz.jey.go4lunch

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.gz.jey.go4lunch.activities.MainActivity
import com.gz.jey.go4lunch.models.Place
import com.gz.jey.go4lunch.utils.ApiStreams
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class RequestTest {


    @get:Rule
    val mActivityRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    private var mActivity: MainActivity? = null

    /**
     * The setUp
     */
    @Before
    fun setUp() {
        mActivity = mActivityRule.activity
    }

    /**
     * test the Restaurants Request
     */
    @Test
    fun testRestaurantsRequest() {

        val key = mActivity!!.resources.getString(R.string.google_maps_key)
        val loc = LatLng(45.750000,4.850000) // LYON coordinates
        //1 - Get the stream
        val observableInfos = ApiStreams.streamFetchRestaurants(key,loc,0)
        //2 - Call TestRequest function to test this TopStories  Request
        testRequest(observableInfos)
    }

    /**
     * @param observableInfos Observable<Place>
     * The test with checking status from Place model once request returned
     */
    private fun testRequest(observableInfos: Observable<Place>) {
        //1 - Create a new TestObserver
        val testObserver = TestObserver<Place>()
        //2 - Launch observable
        observableInfos.subscribeWith<TestObserver<Place>>(testObserver)
                .assertNoErrors() // 3.1 - Check if no errors
                .assertNoTimeout() // 3.2 - Check if no Timeout
                .awaitTerminalEvent() // 3.3 - Await the stream terminated before continue

        //3 - Get news fetched
        val newsFetched = testObserver.values()[0]

        //4 - Verify if Status is not null
        assertNotNull(newsFetched.status)
    }
}