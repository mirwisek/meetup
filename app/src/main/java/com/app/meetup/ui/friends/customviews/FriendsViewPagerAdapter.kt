package com.app.meetup.ui.friends.customviews

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.meetup.ui.friends.FriendsListFragment
import com.app.meetup.ui.friends.FriendsPendingRequestsFragment
import com.app.meetup.ui.friends.FriendsRequestsFragment

class FriendsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
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