package com.app.meetup.ui.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.*
import com.app.meetup.ui.contacts.customviews.FriendsListRecyclerAdapter
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.utils.getPhoneNoFormatted
import com.app.meetup.utils.toastFrag
import kotlinx.android.synthetic.main.fragment_friends_list.view.*

class FriendsPendingRequestsFragment : Fragment() {

    private lateinit var vmActivity: ActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vmActivity = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        val adapter = FriendsListRecyclerAdapter(requireContext())
        view.rvFriendsList.adapter = adapter

        vmActivity.requestSent.observe(viewLifecycleOwner, {
            it?.let { accounts ->
                adapter.updateList(accounts)
            }
        })

        adapter.setOnRequestReactionListener(object: FriendsListRecyclerAdapter.OnRequestReaction {

            override fun onRequestCancelled(account: Account, index: Int) {

                FirestoreUtils.cancelPendingRequest(getPhoneNoFormatted()!!, account.profile.phoneNo)
                    .addOnFailureListener {
                        toastFrag("Couldn't cancel request, try again later.")
                        it.printStackTrace()
                    }
            }

        })
    }

}