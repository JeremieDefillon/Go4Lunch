package com.gz.jey.go4lunch.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Contact

class DetailsAdapter
/**
 * @param contacts List<Contact>
 * @param glide RequestManager
 * @param callback Listener */
(// FOR DATA
        private val contacts: List<Contact>,
        private val glide: RequestManager,
        // FOR COMMUNICATION
        private val callback: Listener) : RecyclerView.Adapter<DetailsViewHolder>() {

    interface Listener

    private var context : Context? = null

    /**
     * @param parent ViewGroup
     * @param viewType int
     * @return com.gz.jey.go4lunch.views.DetailsViewHolder(View)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.workmates_item, parent, false)
        return DetailsViewHolder(view)
    }

    /**
     * @param viewHolder com.gz.jey.go4lunch.views.DetailsViewHolder
     * @param position int
     * UPDATE VIEW HOLDER WITH NEWS
     */
    override fun onBindViewHolder(viewHolder: DetailsViewHolder, position: Int) {
        viewHolder.updateDetails(context!!, this.contacts[position], this.callback)
    }

    /**
     * @return THE TOTAL COUNT OF ITEMS IN THE LIST
     */
    override fun getItemCount(): Int {
        return this.contacts.size
    }

}
