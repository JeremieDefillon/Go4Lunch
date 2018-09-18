package com.gz.jey.go4lunch.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Tasks
import com.gz.jey.go4lunch.models.Details
import com.gz.jey.go4lunch.utils.ApiStreams
import io.reactivex.observers.DisposableObserver
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


@Suppress("UNCHECKED_CAST")
class PlacesAdapter(context: Context, resourceId: Int, geoData: GeoDataClient, filter: AutocompleteFilter?, boundS_GREATER_SYDNEY: LatLngBounds) : ArrayAdapter<AutocompletePrediction>(context, resourceId), Filterable {

    var resultList: MutableList<AutocompletePrediction> = ArrayList()
    private val TAG = "PlaceAutoAdapter"
    private val mContext = context
    private val bounds = boundS_GREATER_SYDNEY

    private val geoDataClient = geoData
    private val mPlaceFilter = filter


    /**
     * GET RESULT SIZE
     * @return Int
     */
    override fun getCount(): Int {
        return resultList.size
    }

    /**
     * GET ITEM
     * @param position Int
     * @return AutocompletePrediction
     */
    override fun getItem(position: Int): AutocompletePrediction? {
        return resultList[position]
    }

    /**
     * GET VIEW
     * @param position Int
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        val textView1 = row.findViewById<View>(android.R.id.text1) as TextView
        textView1.text = item?.getFullText(null)

        return row
    }

    /**
     * GET FILTER
     * @return Filter
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()

                // We need a separate list to store the results, since
                // this is run asynchronously.
                var filterData: ArrayList<AutocompletePrediction>? = ArrayList()

                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(constraint)
                }

                results.values = filterData
                if (filterData != null) {
                    results.count = filterData.size
                } else {
                    results.count = 0
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                resultList.clear()
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    val testList: MutableList<AutocompletePrediction> = results.values as ArrayList<AutocompletePrediction>

                    for ((i, r) in testList.withIndex()){
                        ApiStreams.streamFetchDetails(r.placeId!!, 0)
                                .subscribeWith(object : DisposableObserver<Details>() {
                                    override fun onNext(details: Details) {
                                        val fType = details.result!!.types!!
                                        /*for(r in fType){
                                            Log.d("TYPE" , r.toString())
                                        }*/
                                        if(fType.contains("meal_takeaway") || fType.contains("restaurant")) {
                                            Log.d("TYPE" , fType.toString())
                                            resultList.add(r)
                                            notifyDataSetChanged()
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        Log.e("FILTER RESULT RX", e.toString())
                                        if(i==testList.size-1){
                                            Log.d("resultList", "resultList==$resultList")
                                            notifyDataSetChanged()
                                        }
                                    }

                                    override fun onComplete() {
                                        if(i==testList.size-1){
                                            Log.d("resultList", "resultList==$resultList")
                                            notifyDataSetChanged()
                                        }
                                    }
                                })
                    }

                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    /**
     * GET AUTOCOMPLETE
     * @param constraint CharSequence
     * @return ArrayList<AutocompletePrediction>
     */
    private fun getAutocomplete(constraint: CharSequence): ArrayList<AutocompletePrediction>? {
        Log.i(TAG, "Starting autocomplete query for:$constraint")

        // Submit the query to the autocomplete API and retrieve a PendingResult that will
        // contain the results when the query completes.
        val results = geoDataClient.getAutocompletePredictions(constraint.toString(), bounds,
                mPlaceFilter)

        // This method should have been called off the main UI thread. Block and wait for at most
        // 60s for a result from the API.
        try {
            Tasks.await(results, 60, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }

        return try {
            val autocompletePredictions = results.result

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.count
                    + " predictions.")

            // Freeze the results immutable representation that can be stored safely.
            DataBufferUtils.freezeAndClose<AutocompletePrediction, AutocompletePrediction>(autocompletePredictions)
        } catch (e: RuntimeExecutionException) {
            // If the query did not complete successfully return null
            Toast.makeText(mContext, "Error contacting API: " + e.toString(),
                    Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error getting autocomplete prediction API call", e)
            null
        }

    }

}