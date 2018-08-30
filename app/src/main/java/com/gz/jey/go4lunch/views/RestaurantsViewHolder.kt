package com.gz.jey.go4lunch.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.CalculateRate
import com.gz.jey.go4lunch.utils.SetImageColor
import java.lang.ref.WeakReference

class RestaurantsViewHolder
/**
 * @param itemView View
 */
internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var name: TextView? = null
    private var address: TextView? = null
    private var openTime: TextView? = null

    private var distance: TextView? = null
    private var workmatesIcon: ImageView? = null
    private var workmatesAmount: TextView? = null
    private var firstStar: ImageView? = null
    private var secondStar: ImageView? = null
    private var thirdStar: ImageView? = null

    private var restaurantImg: ImageView? = null

    private var callbackWeakRef: WeakReference<RestaurantsAdapter.Listener>? = null

    init {
        name = itemView.findViewById(R.id.name)
        address = itemView.findViewById(R.id.address)
        openTime = itemView.findViewById(R.id.opening_time)

        distance = itemView.findViewById(R.id.distance)
        workmatesIcon = itemView.findViewById(R.id.num_workmates_icon)
        workmatesAmount = itemView.findViewById(R.id.workmates_amount)
        firstStar = itemView.findViewById(R.id.first_star)
        secondStar = itemView.findViewById(R.id.second_star)
        thirdStar = itemView.findViewById(R.id.third_star)

        restaurantImg = itemView.findViewById(R.id.restaurant_image)
    }

    /**
     * @param res Result
     * @param glide RequestManager
     * @param callback NewsAdapter.Listener
     * UPDATE NEWS ITEM LIST
     */
    fun updateRestaurants(key: String, context : Context, startLatLng: LatLng, res: Result, callback: RestaurantsAdapter.Listener) {

        this.address!!.text = res.formattedAddress
        this.name!!.text = res.name
        this.openTime!!.text = if(res.openingHours==null || res.openingHours.openNow==null) "" else if(res.openingHours.openNow) "Open" else "Closed"
        setTime(context, this.openTime!!.text as String)
        val dist = SphericalUtil.computeDistanceBetween(startLatLng, LatLng(res.geometry.location.lat, res.geometry.location.lng))
        this.distance!!.text = getDistance(dist)
        this.workmatesAmount!!.text = "(0)"
        this.workmatesIcon!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.perm_identity, Color.BLACK))
        when(CalculateRate.getRateOn3(res.rating)){
            1 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
            }
            2 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.secondStar!!.visibility = View.VISIBLE
            }
            3 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.firstStar!!.visibility = View.VISIBLE
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.secondStar!!.visibility = View.VISIBLE
                this.thirdStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.thirdStar!!.visibility = View.VISIBLE
            }
        }

        val imgLink = ApiPhoto.getPhotoURL(100, res.photos[0].photoReference, key)
        GlideApp.with(context)
                .load(imgLink)
                .into(restaurantImg!!)


        callbackWeakRef = WeakReference(callback)
    }

    private fun getDistance(dist : Double) : String{
        return if(dist>999)
            (dist/1000).toInt().toString()+" km"
        else
            dist.toInt().toString()+" m"
    }

    private fun setTime(context : Context, time : String){
        when(time){
            "Open" -> {this.openTime!!.setTextColor(ContextCompat.getColor(context, R.color.colorOpen))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.openTime!!.setTextAppearance(BOLD)
                }}
            "Closed" ->{
                    this.openTime!!.setTextColor(ContextCompat.getColor(context, R.color.colorClosed))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        this.openTime!!.setTextAppearance(ITALIC)
                    }
                }
        }
    }

    /**
     * @param view View
     * OnClick callback
     */
    override fun onClick(view: View) {
        callbackWeakRef!!.get()
    }

}