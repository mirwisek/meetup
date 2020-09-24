package com.app.meetup.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.meetup.R
import com.app.meetup.utils.gone
import com.app.meetup.utils.visible
import kotlinx.android.synthetic.main.fragment_notifications.view.*

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsViewModel =
            ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)

        val adapter = NotificationsRecyclerAdapter(requireContext())
        view.rvNotifications.adapter = adapter

        notificationsViewModel.notificationList.observe(viewLifecycleOwner) {
            it?.let { list ->
                if(list.isNullOrEmpty())
                    view.placeHolder.visible()
                else {
                    view.placeHolder.gone()
                    adapter.updateList(list)
                }
            }
        }

    }
}