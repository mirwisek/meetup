package com.app.meetup.ui.home.eventlist

import android.R.attr.*
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.meetup.R
import com.app.meetup.ui.home.combineFormat
import com.app.meetup.ui.home.models.Event
import com.app.meetup.utils.log
import com.app.meetup.utils.toPx
import com.google.android.material.button.MaterialButton


class EventsListRecyclerAdapter(context: Context): RecyclerView.Adapter<EventsListRecyclerAdapter.ViewHolder>() {

    private var list: List<Event>? = null
    private val ctx: Context = context
    private var clickListener: OnItemClickListener? = null


    private val dpX = ctx.toPx(100)
    private val dpDefault = ctx.toPx(10)
    private val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

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

        // Set extra top padding for Last item blocked by FAB

        if(position == list?.size?.minus(1)) {
            params.setMargins(dpDefault, dpDefault, dpDefault, dpX)
        } else {
            params.setMargins(dpDefault, dpDefault, dpDefault, 0)
        }

        holder.container.layoutParams = params

        holder.eventTitle.text = event.eventTitle
        holder.locationName.text = event.selectedVenue.locationName
        holder.eventTime.text = event.startTime.combineFormat(event.endTime)
        holder.organizerName.text = event.organizer.name

        holder.itemView.setOnClickListener {
            clickListener?.onClicked(event, position)
        }

        holder.delete.setOnClickListener {
            holder.itemView.alpha = 0.7F
            holder.delete.isEnabled = false
            clickListener?.onDeleted(event, position)
            try {
                Handler().postDelayed({
                    holder.delete.isEnabled = true
                    holder.itemView.alpha = 1F
                }, 1000)
            } catch (e: Exception) {
                log("Handler exception in enabling delete")
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val eventTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val locationName: TextView = view.findViewById(R.id.locationName)
        val eventTime: TextView = view.findViewById(R.id.tvEventTime)
        val organizerName: TextView = view.findViewById(R.id.tvOrganizerName)
        val image: ImageView = view.findViewById(R.id.eventPlaceImage)

        val delete: MaterialButton = view.findViewById(R.id.btnDelete)
        val container: View = view.findViewById(R.id.container)
    }

    fun updateList(newList: List<Event>){
        list = newList
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onClicked(event: Event, index: Int)
        fun onDeleted(event: Event, index: Int)
    }
}