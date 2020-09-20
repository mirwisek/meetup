package com.app.meetup.ui.contacts.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.meetup.Account
import com.app.meetup.R
import com.google.android.material.button.MaterialButton

class FriendsListRecyclerAdapter(context: Context): RecyclerView.Adapter<FriendsListRecyclerAdapter.ViewHolder>() {

    private var list: List<Account>? = null
    private val ctx: Context = context
    private var friendReactionListener: OnFriendReaction? = null
    private var reqReactionListener: OnRequestReaction? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_friends_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val account = list!![position]

        // Set extra top padding for First item
//        if(position == 0) {
//            val value = ctx.resources.getDimension(R.dimen.padding_item_accounts).toInt()
//            val dpX = value + ctx.toPx(10)
//            holder.binding.containerItemAccounts.setPadding(0, dpX, 0, value)
//        }

        holder.name.text = account.profile.name
        holder.contactName.text = account.profile.phoneNo
        holder.btnReaction.text = if(account.isFriend)
            ctx.getString(R.string.unfriend)
        else {
            if(account.isRequestSent)
                ctx.getString(R.string.cancel_request)
            else
                ctx.getString(R.string.add_friend)
        }


        holder.btnReaction.setOnClickListener {
            if(account.isFriend)
                friendReactionListener?.onUnfriend(account, position)
            else {
                if(account.isRequestSent)
                    reqReactionListener?.onRequestCancelled(account, position)
                else
                    friendReactionListener?.onAddedFriend(account, position)
            }

        }
    }

    fun setOnFriendReactionListener(listener: OnFriendReaction) {
        friendReactionListener = listener
    }

    fun setOnRequestReactionListener(listener: OnRequestReaction) {
        reqReactionListener = listener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val name: TextView = view.findViewById(R.id.tvFriendName)
        val contactName: TextView = view.findViewById(R.id.tvContactName)
        val btnReaction: MaterialButton = view.findViewById(R.id.btnReaction)
    }

    fun updateList(newList: List<Account>){
        list = newList
        notifyDataSetChanged()
    }

    interface OnFriendReaction {
        fun onAddedFriend(account: Account, index: Int)
        fun onUnfriend(account: Account, index: Int)
    }

    interface OnRequestReaction {
        fun onRequestCancelled(account: Account, index: Int)
    }
}