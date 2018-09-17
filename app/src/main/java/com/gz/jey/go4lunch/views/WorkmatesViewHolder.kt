package com.gz.jey.go4lunch.views

import android.content.Context
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Contact
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.ref.WeakReference

class WorkmatesViewHolder
/**
 * @param itemView View
 */
internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var name: TextView? = null
    private var photo: CircleImageView? = null
    private var action: TextView? = null
    private var restaurant: TextView? = null

    private var callbackWeakRef: WeakReference<WorkmatesAdapter.Listener>? = null

    init {
        name = itemView.findViewById(R.id.name)
        photo = itemView.findViewById(R.id.photo)
        action = itemView.findViewById(R.id.action)
        restaurant = itemView.findViewById(R.id.restaurant)
    }

    /**
     * @param context Context
     * @param contact Contact
     * @param callback NewsAdapter.Listener
     * UPDATE NEWS ITEM LIST
     */
    fun updateWorkmates(context : Context, contact: Contact, callback: WorkmatesAdapter.Listener) {

        val goEat = context.getString(R.string.go_eat).toString()
        val notChosen = context.getString(R.string.doesnt_chosen).toString()

        this.name!!.text = contact.username
        this.restaurant!!.text = contact.whereEatName

        if(!contact.whereEatID.isEmpty()){
            this.action!!.text = goEat
            this.action!!.setTextColor(getColor(context,R.color.colorBlack))
            this.name!!.setTextColor(getColor(context,R.color.colorBlack))
            this.restaurant!!.setTextColor(getColor(context,R.color.colorAccent))
        } else{
            this.action!!.text = notChosen
            this.action!!.setTextColor(getColor(context,R.color.colorGrey))
            this.name!!.setTextColor(getColor(context,R.color.colorGrey))
            this.restaurant!!.setTextColor(getColor(context,R.color.colorGrey))
        }

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