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
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.CalculateRatio
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
    fun updateRestaurants(key: String, context : Context, allContact : Int, startLatLng: LatLng, res: Result, callback: RestaurantsAdapter.Listener) {

        this.address!!.text = res.formattedAddress
        this.name!!.text = res.name
        var open = false
        var oc = ""
        if(res.openingHours==null || res.openingHours.openNow==null){
            oc = ""
        }
        else if(res.openingHours.openNow) {
            oc = context.getString(R.string.open)
            open = true
        }
        else {
            oc = context.getString(R.string.close)
            open = false
        }

        this.openTime!!.text = oc
        setTime(context, open)
        val dist = SphericalUtil.computeDistanceBetween(startLatLng, LatLng(res.geometry.location.lat, res.geometry.location.lng))
        this.distance!!.text = getDistance(dist)
        val amount = res.workmates.size
        this.workmatesAmount!!.text = "($amount)"
        this.workmatesIcon!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.perm_identity, Color.BLACK))
        when(CalculateRatio.getRateOn3(res.rating)){
            1 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorTransparent)))
                this.thirdStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorTransparent)))
            }
            2 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.thirdStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorTransparent)))
            }
               3 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
                this.thirdStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary)))
            }
        }

        when(CalculateRatio.getLike(res.liked, allContact)){
            1 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))}
            2 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))
            }
            3 -> {this.firstStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))
                this.secondStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))
                this.thirdStar!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent)))
            }
        }

        val imgLink = ApiPhoto.getPhotoURL(100, res.photos[0].photoReference, key)
        Glide.with(context)
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

    private fun setTime(context : Context, open : Boolean){
        if(open){
            this.openTime!!.setTextColor(ContextCompat.getColor(context, R.color.colorOpen))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this.openTime!!.setTextAppearance(BOLD)
        }else{
            this.openTime!!.setTextColor(ContextCompat.getColor(context, R.color.colorClosed))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this.openTime!!.setTextAppearance(ITALIC)
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