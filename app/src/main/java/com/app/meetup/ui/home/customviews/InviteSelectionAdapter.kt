package com.app.meetup.ui.home.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.meetup.Invite
import com.app.meetup.R
import com.app.meetup.utils.toPx

class InviteSelectionAdapter(context: Context): RecyclerView.Adapter<InviteSelectionAdapter.ViewHolder>() {

    private var list: List<Invite>? = null
    private val ctx: Context = context
    private var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_invite_selection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val invite = list!![position]
        val profile = invite.profile

        // Set extra top padding for First item
        if(position == 0) {
            val value = ctx.resources.getDimension(R.dimen.padding_item_contacts).toInt()
            val dpX = value + ctx.toPx(10)
            holder.parentLayout.setPadding(0, dpX, 0, value)
        }

        holder.name.text = profile.name
        holder.contactName.text = profile.phoneNo

        holder.imgCheckIcon.visibility = if(invite.isInvited) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener {
            // Invert logic down below
//            holder.imgCheckIcon.visibility = if(holder.imgCheckIcon.isVisible) View.INVISIBLE else View.VISIBLE
            listener?.onClicked(invite, position)
        }
    }

    fun setOnClickListener(clickListener: OnItemClickListener) {
        listener = clickListener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val name: TextView = view.findViewById(R.id.profileName)
        val contactName: TextView = view.findViewById(R.id.contactName)
        val imgCheckIcon: ImageView = view.findViewById(R.id.check)

        val parentLayout: ConstraintLayout = view.findViewById(R.id.containerItemAccounts)
    }

    fun updateList(newList: List<Invite>){
        list = newList
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onClicked(invite: Invite, index: Int)
    }
}