package com.app.meetup.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.ActivityViewModel
import com.app.meetup.LoginActivity
import com.app.meetup.R
import com.app.meetup.ui.contacts.customviews.ContactsViewPagerAdapter
import com.app.meetup.utils.Constants
import com.app.meetup.utils.gone
import com.app.meetup.utils.visible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_friends_list.view.*

class ContactsFragment : Fragment() {

    private lateinit var contactsViewModel: ContactsViewModel
    private val tabTitles = arrayListOf("Friends", "Requests", "Pending")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_contacts, container, false)

        val pagerAdapter = ContactsViewPagerAdapter(parentFragmentManager, lifecycle)

        root.friendsViewPager.adapter = pagerAdapter

        TabLayoutMediator(root.friendsTabLayout, root.friendsViewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = tabTitles[i]
            root.friendsViewPager.setCurrentItem(tab.position, true)
        }.attach()

        root.toolbar_layout.apply {
            inflateMenu(R.menu.contacts_menu)

            title = "Contacts on Meetup"
            subtitle = "Your contacts sync automatically"

            setOnMenuItemClickListener { menuItem ->
                if(menuItem.itemId == R.id.logout) {

                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }

                false
            }
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        vm.allContacts.observe(viewLifecycleOwner) {
            it?.let {
                view.progressContacts.gone()
                vm.allContacts.removeObservers(viewLifecycleOwner)
            }
        }

        vm.empty.observe(viewLifecycleOwner) {
            it?.let { map ->
                map[Constants.USERDATA]?.let { v ->
                    if(v) {
                        view.progressContacts.gone()
                    }
                }
            }
        }


        vm.friendRequests.observe(viewLifecycleOwner, {
            it?.let { accounts ->
                view.friendsTabLayout.getTabAt(1)?.text = if(accounts.size > 0) {
                    "Requests (${accounts.size})"
                } else {
                    "Requests"
                }
            }
        })

        vm.requestSent.observe(viewLifecycleOwner, {
            it?.let { accounts ->
                view.friendsTabLayout.getTabAt(2)?.text = if(accounts.size > 0) {
                    "Pending (${accounts.size})"
                } else {
                    "Pending"
                }
            }
        })
    }
}