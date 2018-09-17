package com.gz.jey.go4lunch.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Contact
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
     * @param context Context
     * @param contact Contact
     * @param callback NewsAdapter.Listener
     * UPDATE NEWS ITEM LIST
     */
    fun updateDetails(context : Context, contact: Contact, callback: DetailsAdapter.Listener) {

        this.name!!.text = contact.username
        this.action!!.text = context.getString(R.string.is_joining).toString()

        val imgLink = contact.urlPicture
        Glide.with(context)
                .load(imgLink)
                .into(photo!!)

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