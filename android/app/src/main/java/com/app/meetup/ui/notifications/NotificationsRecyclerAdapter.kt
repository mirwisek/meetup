package com.app.meetup.ui.notifications

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
import com.app.meetup.ui.notifications.models.Notification
import com.app.meetup.ui.notifications.models.NotificationsList

class NotificationsRecyclerAdapter(context: Context): RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder>() {

    private var list: List<Notification>? = null
    private val ctx: Context = context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list!![position]

        // Set extra top padding for First item
//        if(position == 0) {
//            val dpX = ctx.toPx(10)
//            holder.container.setPadding(0, dpX, 0, dpX)
//        }

        holder.title.text = item.title
        holder.body.text = item.body
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.title)
        val body: TextView = view.findViewById(R.id.body)
    }

    fun updateList(newList: List<Notification>){
        list = newList
        notifyDataSetChanged()
    }

}