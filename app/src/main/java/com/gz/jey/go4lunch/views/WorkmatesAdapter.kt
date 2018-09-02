package com.gz.jey.go4lunch.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.gz.jey.go4lunch.R
import com.gz.jey.go4lunch.models.Contact

class WorkmatesAdapter
/**
 * @param myContacts List<MyContacts>
 * @param glide RequestManager
 * @param callback Listener */
(
        // FOR DATA
        private val contacts: List<Contact>,
        private val glide: RequestManager,
        // FOR COMMUNICATION
        private val callback: Listener) : RecyclerView.Adapter<WorkmatesViewHolder>() {

    interface Listener

    private var context: Context? = null

    /**
     * @param parent ViewGroup
     * @param viewType int
     * @return com.gz.jey.go4lunch.views.WorkmatesViewHolder(View)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkmatesViewHolder {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.workmates_item, parent, false)
        return WorkmatesViewHolder(view)
    }

    /**
     * @param viewHolder com.gz.jey.go4lunch.views.WorkmatesViewHolder
     * @param position int
     * UPDATE VIEW HOLDER WITH NEWS
     */
    override fun onBindViewHolder(viewHolder: WorkmatesViewHolder, position: Int) {
        viewHolder.updateWorkmates(
                context!!,
                this.contacts[position],
                this.callback)
    }

    /**
     * @return THE TOTAL COUNT OF ITEMS IN THE LIST
     */
    override fun getItemCount(): Int {
        return this.contacts.size
    }

    /**
     * @param position int
     * @return Result
     */
    fun getWorkmates(position: Int): Contact {
        return this.contacts[position]
    }
}
