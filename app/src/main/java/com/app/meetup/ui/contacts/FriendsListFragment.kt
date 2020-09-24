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
import com.app.meetup.utils.log
import com.app.meetup.utils.toastFrag
import kotlinx.android.synthetic.main.fragment_friends_list.view.*

class FriendsListFragment : Fragment() {

    private lateinit var vmActivity: ActivityViewModel

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


        vmActivity.newContacts.observe(viewLifecycleOwner, { accounts ->
            log("got accs ${accounts}")
            accounts?.let {
                adapter.updateList(it)
            }
        })

        adapter.setOnFriendReactionListener(object: FriendsListRecyclerAdapter.OnFriendReaction {

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

        })
    }

}