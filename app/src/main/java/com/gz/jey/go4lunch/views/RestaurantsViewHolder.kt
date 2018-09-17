package com.gz.jey.go4lunch.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getDrawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
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
     * @param key String
     * @param context Context
     * @param allContact Int
     * @param res Result
     * @param callback NewsAdapter.Listener
     * UPDATE RESTAURANTS LIST
     */
    fun updateRestaurants(key: String, context : Context, allContact : Int, res: Result, callback: RestaurantsAdapter.Listener) {

        val sb = StringBuilder()
            sb.append("REST NAME = ").append(res.name).append("\r\n")
            sb.append("ADRESS = ").append(res.formatted_address).append("\r\n")
            sb.append("REST NAME = ").append(res.name).append("\r\n")

        Log.d("RESTAURANT CREATED" , sb.toString())

        this.address!!.text = res.formatted_address
        this.name!!.text = res.name
        val open : Boolean
        val oc : String
        when {
            res.opening_hours?.openNow == null -> {
                oc = ""
                open = false
            }
            res.opening_hours.openNow!! -> {
                oc = context.getString(R.string.open)
                open = true
            }
            else -> {
                oc = context.getString(R.string.close)
                open = false
            }
        }

        val googleStar = SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorPrimary))
        val likeStar = SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorAccent))
        val emptyStar = SetImageColor.changeDrawableColor(context, R.drawable.star_rate, ContextCompat.getColor(context, R.color.colorTransparent))

        this.openTime!!.text = oc
        setTime(context, open)
        this.distance!!.text = getDistance(res.distance!!)
        val amount = res.workmates.size
        val amString = "($amount)"
        this.workmatesAmount!!.text = amString
        this.workmatesIcon!!.setImageDrawable(SetImageColor.changeDrawableColor(context, R.drawable.perm_identity, Color.BLACK))
        when(CalculateRatio.getRateOn3(res.rating!!)){
            1 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(emptyStar)
                this.thirdStar!!.setImageDrawable(emptyStar)
            }
            2 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(googleStar)
                this.thirdStar!!.setImageDrawable(emptyStar)
            }
               3 -> {this.firstStar!!.setImageDrawable(googleStar)
                this.secondStar!!.setImageDrawable(googleStar)
                this.thirdStar!!.setImageDrawable(googleStar)
            }
        }

        when(CalculateRatio.getLike(res.liked, allContact)){
            1 -> {this.firstStar!!.setImageDrawable(likeStar)}
            2 -> {this.firstStar!!.setImageDrawable(likeStar)
                this.secondStar!!.setImageDrawable(likeStar)
            }
            3 -> {this.firstStar!!.setImageDrawable(likeStar)
                this.secondStar!!.setImageDrawable(likeStar)
                this.thirdStar!!.setImageDrawable(likeStar)
            }
        }

        if(res.photos!=null) {
            val imgLink = ApiPhoto.getPhotoURL(100, res.photos[0].photo_reference!!, key)
            Glide.with(context)
                    .load(imgLink)
                    .into(restaurantImg!!)
        }else
            restaurantImg!!.background = getDrawable(context, R.drawable.no_pic)

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