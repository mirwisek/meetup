package com.app.meetup.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.meetup.R
import com.app.meetup.ui.friends.customviews.FriendsViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_friends.view.*

class FriendsFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel
    private val tabTitles = arrayListOf("Friends", "Requests", "Pending")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        friendsViewModel =
//            ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_friends, container, false)

        val pagerAdapter = FriendsViewPagerAdapter(parentFragmentManager, lifecycle)

        root.friendsViewPager.adapter = pagerAdapter

        TabLayoutMediator(root.friendsTabLayout, root.friendsViewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = tabTitles[i]
            root.friendsViewPager.setCurrentItem(tab.position, true)
        }.attach()

        return root
    }
}