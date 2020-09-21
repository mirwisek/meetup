package com.app.meetup.ui.home.eventlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.ActivityViewModel
import com.app.meetup.Profile
import com.app.meetup.R
import com.app.meetup.ui.home.HomeViewModel
import com.app.meetup.ui.home.combineTimeFormat
import com.app.meetup.ui.home.formatDate
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_add_event.view.locationName
import kotlinx.android.synthetic.main.fragment_edit_event.view.*
import kotlin.math.abs

class EditEventFragment : Fragment() {

    private lateinit var viewModel: ActivityViewModel
    private lateinit var vmHome: HomeViewModel

    companion object {
        const val KEY_EVENT_ID = "eventIdSelected"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_edit_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)
        vmHome = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        val adapter = VoteListRecyclerAdapter(requireContext())
        view.rvVotes.adapter = adapter

        val eventId = arguments?.get(KEY_EVENT_ID)

        vmHome.events.observe(viewLifecycleOwner, {
            it?.let {  events ->
                val event = events.first { e -> e.id == eventId }

                view.locationName.text = event.eventTitle
                view.guestTitle.text = "${event.invites.size} guests"


                val allVoters = mutableListOf<Profile>()
                event.votes.forEach {  v ->
                    v.voters.forEach {  p -> allVoters.add(p) }
                }

                // Invites doesn't contain (Organizer but votes contain organizer) that's why +1
                val diffVotes = abs(event.invites.size - allVoters.size ) + 1

                view.pendingVotesSubtitle.text = "${allVoters.size} responded, $diffVotes awaiting"
                view.guestSubtitle.text = "${allVoters.size - 1} responded, $diffVotes awaiting"

                val pendingVoters = event.invites - allVoters
                pendingVoters.forEach { profile ->
                    val chip = Chip(requireContext())
                    chip.text = profile.name
                    view.pendingVotesGroup.addView(chip)
                }

                // +1 for organizer (he can also check In)
                val diffCheckedIn = abs(event.checkedIn.size - event.invites.size) + 1
                view.checkedInSubtitle.text = "${event.checkedIn.size} confirmed, $diffCheckedIn awaiting"

                event.checkedIn.forEach { profile ->
                    val chip = Chip(requireContext())
                    chip.text = profile.name
                    view.checkedInGroup.addView(chip)
                }

                view.date.text = event.startTime.toLocalDate().formatDate

                val endTime = event.endTime.toLocalTime()
                view.time.text = event.startTime.toLocalTime().combineTimeFormat(endTime)



                adapter.updateList(event)
            }
        })

//        vmHome.events.observe(viewLifecycleOwner, {
//            it?.let { list ->
//                adapter.updateList(list)
//            }
//        })
    }

}