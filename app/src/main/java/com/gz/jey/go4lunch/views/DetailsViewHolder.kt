package com.gz.jey.go4lunch.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Result
import com.gz.jey.go4lunch.models.Workmates
import com.gz.jey.go4lunch.utils.ApiPhoto
import com.gz.jey.go4lunch.utils.SetBottomMenuTab
import java.lang.Math.round
import java.lang.ref.WeakReference

class DetailsViewHolder
/**
 * @param itemView View
 */
internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var photo: ImageView? = null
    private var name: TextView? = null
    private var action: TextView? = null

    private var callbackWeakRef: WeakReference<DetailsAdapter.Listener>? = null

    init {
        photo = itemView.findViewById(R.id.photo)
        name = itemView.findViewById(R.id.name)
        action = itemView.findViewById(R.id.action)
    }

    /**
     * @param res Result
     * @param glide RequestManager
     * @param callback NewsAdapter.Listener
     * UPDATE NEWS ITEM LIST
     */
    fun updateDetails(key: String, context : Context, startLatLng: LatLng, res: Workmates, callback: DetailsAdapter.Listener) {

        this.name!!.text = res.name
        callbackWeakRef = WeakReference(callback)
    }

        /**
     * @param view View
     * OnClick callback
     */
    override fun onClick(view: View) {
        callbackWeakRef!!.get()
    }

}