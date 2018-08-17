package com.gz.jey.go4lunch.views

import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Color
import android.media.Image
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.ApiStreams
import com.gz.jey.go4lunch.utils.SetBottomMenuTab
import java.lang.ref.WeakReference

class RestaurantsViewHolder
/**
 * @param itemView View
 */
internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.name)
    internal var name: TextView? = null
    @BindView(R.id.address)
    internal var address: TextView? = null
    @BindView(R.id.opening_time)
    internal var openTime: TextView? = null

    @BindView(R.id.distance)
    internal var distance: TextView? = null
    @BindView(R.id.num_workmates_icon)
    internal var workmatesIcon: ImageView? = null
    @BindView(R.id.workmates_amount)
    internal var workmatesAmount: TextView? = null
    @BindView(R.id.first_star)
    internal var firstStar: ImageView? = null
    @BindView(R.id.second_star)
    internal var secondStar: ImageView? = null
    @BindView(R.id.third_star)
    internal var thirdStar: ImageView? = null

    @BindView(R.id.restaurant_image)
    internal var restaurantImg: ImageView? = null

    private var callbackWeakRef: WeakReference<RestaurantsAdapter.Listener>? = null

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * @param res Result
     * @param glide RequestManager
     * @param callback NewsAdapter.Listener
     * UPDATE NEWS ITEM LIST
     */
    fun updateRestaurants(key: String, context : Context, startLatLng: LatLng, res: Result, glide: RequestManager, callback: RestaurantsAdapter.Listener) {


        this.name!!.text = res.name
        this.address!!.text = res.formattedAddress
        this.openTime!!.text = if(res.openingHours.openNow) "Open" else "Closed"
        val dist = StringBuilder()
        dist.append(SphericalUtil.computeDistanceBetween(startLatLng, LatLng(res.geometry.location.lat, res.geometry.location.lng))).append("m")
        this.distance!!.text = dist
        this.workmatesAmount!!.text = "(0)"
        this.workmatesIcon!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.perm_identity, Color.BLACK))
        when(getRateOn3(res.rating)){
            1 -> this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))
            2 -> {this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))
            this.secondStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))}
            3 -> {this.firstStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))
            this.secondStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))
            this.thirdStar!!.setImageDrawable(SetBottomMenuTab.changeDrawableColor(context, R.drawable.star_rate, Color.YELLOW))}
        }

        val img_link = ApiPhoto.getPhotoURL(80, res.photos[0].photoReference, key)
        glide.load(img_link).into(restaurantImg)

        callbackWeakRef = WeakReference(callback)
    }

    private fun getRateOn3(rate : Double) : Int{
        return  ((rate/5)*3) as Int
    }

    /**
     * @param view View
     * OnClick callback
     */
    override fun onClick(view: View) {
        callbackWeakRef!!.get()
    }

}