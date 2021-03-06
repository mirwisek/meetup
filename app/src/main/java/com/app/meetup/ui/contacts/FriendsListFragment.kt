package com.app.meetup.ui.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.Account
import com.app.meetup.ActivityViewModel
import com.app.meetup.R
import com.app.meetup.ui.contacts.customviews.FriendsListRecyclerAdapter
import com.app.meetup.utils.*
import kotlinx.android.synthetic.main.fragment_friends_list.view.*


class FriendsListFragment : Fragment() {

    private lateinit var vmActivity: ActivityViewModel
    private var inviteMessage: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_friends_list, container, false)

        vmActivity = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val adapter = FriendsListRecyclerAdapter(requireContext())
        view.rvFriendsList.adapter = adapter

        vmActivity.empty.observe(viewLifecycleOwner) {
            it?.let { map ->
                map[Constants.USERDATA]?.let { v ->
                    if(v) {
                        view.placeHolder.visible()
                    }
                }
            }
        }


        vmActivity.invitesList.observe(viewLifecycleOwner) { accounts ->
            accounts?.let {
                if(it.isEmpty())
                    view.placeHolder.visible()
                else {
                    view.placeHolder.gone()
                }
                adapter.updateList(it)
            }
        }

        vmActivity.inviteTemplate.observe(viewLifecycleOwner) { msg ->
            inviteMessage = msg ?: getString(R.string.default_invite_msg)
        }

        adapter.setOnFriendReactionListener(object : FriendsListRecyclerAdapter.OnFriendReaction {

            override fun onAddedFriend(account: Account, index: Int) {
                FirestoreUtils.addFriend(getPhoneNoFormatted()!!, account.profile.phoneNo)
                    .addOnSuccessListener {

                        val updatedContact = account.copy(isRequestSent = true)
                        vmActivity.updateContactStatus(account, updatedContact, index)
                    }
                    .addOnFailureListener {
                        toastFrag("Couldn't add friend, server problem")
                        it.printStackTrace()
                    }
            }

            override fun onUnfriend(account: Account, index: Int) {

                FirestoreUtils.unFriend(getPhoneNoFormatted()!!, account.profile.phoneNo)
                    .addOnFailureListener {
                        toastFrag("Couldn't unfriend, server problem")
                        it.printStackTrace()
                    }
            }

            override fun onInvite(account: Account) {
                val uri: Uri = Uri.parse("smsto:${account.profile.phoneNo}")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra("sms_body",inviteMessage)
                startActivity(intent)
            }

        })
    }

}