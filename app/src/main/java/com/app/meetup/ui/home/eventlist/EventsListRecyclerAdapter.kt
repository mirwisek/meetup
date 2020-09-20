package com.app.meetup.ui.home.eventlist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.meetup.R
import com.app.meetup.ui.home.combineFormat
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.EventView

class EventsListRecyclerAdapter(context: Context): RecyclerView.Adapter<EventsListRecyclerAdapter.ViewHolder>() {

    private var list: List<Event>? = null
    private val ctx: Context = context
    private var clickListener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_events_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val event = list!![position]

        // Set extra top padding for First item
//        if(position == 0) {
//            val dpX = ctx.toPx(10)
//            holder.container.setPadding(0, dpX, 0, dpX)
//        }

        holder.eventTitle.text = event.eventTitle
        holder.locationName.text = event.selectedVenue.locationName
        holder.eventTime.text = event.startTime.combineFormat(event.endTime)
        holder.hostName.text = event.organizer.name
        holder.hostPhone.text = event.organizer.phoneNo

        holder.itemView.setOnClickListener {
            clickListener?.onClicked(event, position)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val eventTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val locationName: TextView = view.findViewById(R.id.locationName)
        val eventTime: TextView = view.findViewById(R.id.tvEventTime)
        val hostName: TextView = view.findViewById(R.id.tvHostName)
        val hostPhone: TextView = view.findViewById(R.id.tvHostPhone)
        val image: ImageView = view.findViewById(R.id.eventPlaceImage)
        val container: View = view.findViewById(R.id.container)
    }

    fun updateList(newList: List<Event>){
        list = newList
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onClicked(event: Event, index: Int)
    }
}