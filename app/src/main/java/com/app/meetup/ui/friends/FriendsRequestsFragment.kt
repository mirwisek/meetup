package com.app.meetup.ui.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.*
import com.app.meetup.ui.friends.customviews.FriendsRequestRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_friends_list.view.*

class FriendsRequestsFragment : Fragment() {

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


        val adapter = FriendsRequestRecyclerAdapter(requireContext())
        view.rvFriendsList.adapter = adapter

        vmActivity.friendRequests.observe(viewLifecycleOwner, {
            it?.let { accounts ->
                adapter.updateList(accounts)
            }
        })

        adapter.setOnReactionListener(object: FriendsRequestRecyclerAdapter.OnRequestInteraction {

            override fun onAccepted(account: Account) {
                FirestoreUtils.acceptFriendRequest(getPhoneNoFormatted(), account.profile.phoneNo)
                    .addOnFailureListener {
                        toastFrag("Couldn't accept friend request at the moment, please try again later")
                        it.printStackTrace()
                    }
            }

            override fun onRejected(account: Account) {
                FirestoreUtils.declineFriendRequest(getPhoneNoFormatted(), account.profile.phoneNo)
                    .addOnFailureListener {
                        toastFrag("Couldn't decline friend request at the moment, please try again later")
                        it.printStackTrace()
                    }
            }
        })
    }

}