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
import com.app.meetup.ui.home.models.Venue
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class VoteListRecyclerAdapter(context: Context): RecyclerView.Adapter<VoteListRecyclerAdapter.ViewHolder>() {

    private var event: Event? = null
    private val ctx: Context = context
    private var clickListener: OnItemClickListener? = null

    private var isChecked = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_vote, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return event?.venues?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val venue = event!!.venues[position]

        // Set extra top padding for First item
//        if(position == 0) {
//            val dpX = ctx.toPx(10)
//            holder.container.setPadding(0, dpX, 0, dpX)
//        }

        val vote = event!!.votes.first { v -> v.placeId == venue.id }

        holder.locationName.text = venue.locationName
        holder.address.text = venue.address
        holder.voteCount.text = vote.count.toString()

        vote.voters.forEach { profile ->
            val chip = Chip(ctx)
            chip.text = profile.name
            holder.votersGroup.addView(chip)
        }


        holder.itemView.setOnClickListener {
            clickListener?.onClicked(venue, position)
            isChecked = !isChecked
            holder.checked.visibility = if(isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val locationName: TextView = view.findViewById(R.id.locationName)
        val voteCount: TextView = view.findViewById(R.id.voteCount)
        val address: TextView = view.findViewById(R.id.address)
        val votersGroup: ChipGroup = view.findViewById(R.id.votersGroup)
        val checked: ImageView = view.findViewById(R.id.checked)
    }

    fun updateList(event: Event){
        this.event = event
        notifyDataSetChanged()
    }

    fun toggleCheck() {

    }


    interface OnItemClickListener {
        fun onClicked(venue: Venue, index: Int)
    }
}