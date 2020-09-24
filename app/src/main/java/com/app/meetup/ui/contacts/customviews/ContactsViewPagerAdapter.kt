package com.app.meetup.ui.contacts.customviews

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.meetup.ui.contacts.FriendsListFragment
import com.app.meetup.ui.contacts.FriendsPendingRequestsFragment
import com.app.meetup.ui.contacts.FriendsRequestsFragment

class ContactsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FriendsListFragment()
            1 -> FriendsRequestsFragment()
            else -> FriendsPendingRequestsFragment()
        }
    }
}