package com.app.meetup.ui.contacts.customviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.meetup.*
import com.app.meetup.utils.toPx

class FriendsRequestRecyclerAdapter(context: Context): RecyclerView.Adapter<FriendsRequestRecyclerAdapter.ViewHolder>() {

    private var list: List<Account>? = null
    private val ctx: Context = context
    private var mListener: OnRequestInteraction? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_friends_request, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val account = list!![position]

        // Set extra top padding for First item
        if(position == 0) {
            val dpX = ctx.toPx(10)
            holder.itemView.setPadding(0, dpX, 0, holder.itemView.paddingBottom)
        }

        holder.friendName.text = account.profile.name
        holder.phone.text = account.profile.phoneNo

        holder.btnApprove.setOnClickListener {
            mListener?.onAccepted(account)
        }

        holder.btnDecline.setOnClickListener {
            mListener?.onRejected(account)
        }

//        val phoneDrawable = ctx.getDrawableCompat(R.drawable.fui_ic_phone_white_24dp)
//        phoneDrawable?.let {
//            val drawable = DrawableCompat.wrap(phoneDrawable)
//            DrawableCompat.setTint(drawable.mutate(), ctx.getColorCompat(R.color.colorPrimary))
//            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
//            holder.phone.setCompoundDrawables(drawable, null, null, null)
//        }
    }

    fun setOnReactionListener(listener: OnRequestInteraction) {
        mListener = listener
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val friendName: TextView = view.findViewById(R.id.tvFriendName)
        val phone: TextView = view.findViewById(R.id.tvPhone)
        val btnApprove: ImageButton = view.findViewById(R.id.btnApprove)
        val btnDecline: ImageButton = view.findViewById(R.id.btnDecline)
    }

    fun updateList(newList: List<Account>){
        list = newList
        notifyDataSetChanged()
    }

    interface OnRequestInteraction {
        fun onAccepted(account: Account)
        fun onRejected(account: Account)
    }
}