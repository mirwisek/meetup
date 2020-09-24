package com.app.meetup.ui.home.eventlist

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.app.meetup.ActivityViewModel
import com.app.meetup.Profile
import com.app.meetup.R
import com.app.meetup.ui.home.HomeViewModel
import com.app.meetup.ui.home.addevent.AddEventFragment
import com.app.meetup.ui.home.addevent.AddTitleBottomSheet
import com.app.meetup.ui.home.combineTimeFormat
import com.app.meetup.ui.home.customviews.SingleCheckMaterialButton
import com.app.meetup.ui.home.formatDate
import com.app.meetup.ui.home.isEventOngoing
import com.app.meetup.ui.home.models.Venue
import com.app.meetup.ui.home.models.Vote
import com.app.meetup.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_add_event.view.locationName
import kotlinx.android.synthetic.main.fragment_edit_event.view.*
import kotlin.math.abs

class EditEventFragment : Fragment() {

    private lateinit var viewModel: ActivityViewModel
    private lateinit var vmHome: HomeViewModel
    private var hasCurrentUserVoted = false
    private var title: String? = null

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

        val eventId = arguments?.getString(KEY_EVENT_ID)
        val ctx = requireContext()

        val currentProfile = vmHome.currentProfile.value!!

        val bottomSheet = AddTitleBottomSheet()

        bottomSheet.setOnTitleAddedListener(object : AddTitleBottomSheet.OnTitleAddedListener {
            override fun onAdded(title: String) {
                FirestoreUtils.updateTitle(eventId!!, title).addOnFailureListener {
                    toastFrag("Couldn't update title of event")
                }
            }
        })

        view.fabEditTitle.setOnClickListener {
//            bottomSheet.arguments = Bundle().apply {
//                putString(AddTitleBottomSheet.KEY_TITLE, title)
//            }
            bottomSheet.show(parentFragmentManager, AddTitleBottomSheet.TAG)
        }

        vmHome.events.observe(requireActivity(), {
            it?.let {  events ->
                val event = events.first { e -> e.id == eventId }

                title = event.eventTitle
                vmHome.currentEventTitle.postValue(title)

                view.locationName.text = event.eventTitle
                view.guestTitle.text = "${event.invites.size} guests"


                val allVoters = mutableListOf<Profile>()
                event.votes.forEach {  v ->

                    v.voters.forEach {  p ->
                        // Since we are looping this list any way so I put it here to decided
                        // the button should be New Place or Add Place
                        if(p.phoneNo == currentProfile.phoneNo)
                            hasCurrentUserVoted = true
                        allVoters.add(p)
                    }
                }

                // Only organizer can edit the title
                if(currentProfile.phoneNo != event.organizer.phoneNo)
                    view.fabEditTitle.gone()

                view.btnAddVenue.text = if(hasCurrentUserVoted) "Edit" else "New"

                // Invites doesn't contain (Organizer but votes contain organizer) that's why +1
                val diffVotes = abs(event.invites.size - allVoters.size ) + 1

                view.pendingVotesSubtitle.text = "${allVoters.size} responded, $diffVotes awaiting"
                view.guestSubtitle.text = "${allVoters.size - 1} responded, $diffVotes awaiting"

                view.pendingVotesGroup.removeAllViews()

                val pendingVoters = event.invites - allVoters
                pendingVoters.forEach { profile ->
                    val chip = Chip(ctx)
                    chip.text = profile.name
                    view.pendingVotesGroup.addView(chip)
                }

                // +1 for organizer (he can also check In)
                val diffCheckedIn = abs(event.checkedIn.size - event.invites.size) + 1
                view.checkedInSubtitle.text = "${event.checkedIn.size} confirmed, $diffCheckedIn awaiting"

                view.checkedInGroup.removeAllViews()

                var hasUserCheckedAlready = false
                event.checkedIn.forEach { profile ->
                    val chip = Chip(ctx)
                    chip.text = profile.name
                    view.checkedInGroup.addView(chip)

                    if(profile.phoneNo == currentProfile.phoneNo)
                        hasUserCheckedAlready = true
                }

                view.btnCheckIn.isChecked = hasUserCheckedAlready


                // UI management
                view.btnCheckIn.setOnTouchListener { v, e ->
                    if(e.action == KeyEvent.ACTION_DOWN) {
                        if(hasUserCheckedAlready) {
                            toastFrag("You have already check in")
                            true
                        } else {
                            if(hasCurrentUserVoted) {
                                if(event.startTime.isEventOngoing(event.endTime)) {
                                    v.performClick()
                                    false
                                } else {
                                    toastFrag("You can't check in at least 12h before event start",
                                        Toast.LENGTH_LONG)
                                    true
                                }
                            } else {
                                toastFrag("Please vote first for a location")
                                true
                            }
                        }
                    } else
                        false
                }

                view.btnCheckIn.setOnClickListener {
                    FirestoreUtils.checkIn(eventId!!, currentProfile.phoneNo).addOnFailureListener {
                        toastFrag("Couldn't check in server error")
                    }.addOnSuccessListener {
                        hasUserCheckedAlready = true
                    }
                }

                view.date.text = event.startTime.toLocalDate().formatDate

                val endTime = event.endTime.toLocalTime()
                view.time.text = event.startTime.toLocalTime().combineTimeFormat(endTime)

                adapter.setOnItemClickListener(object: VoteListRecyclerAdapter.OnItemClickListener {

                    override fun onClicked(vote: Vote, index: Int) {

                        vmHome.currentProfile.value?.let { currentProfile ->
                            // Remove currentUser from all other voters Set and add it to clicked item
                            event.votes.forEachIndexed { i, v ->
                                if(index == i)
                                    v.voters.add(currentProfile)
                                else
                                    v.voters.remove(currentProfile)
                            }
                            adapter.updateList(event)
                        }
                    }

                })

                adapter.updateList(event)
            }
        })

        view.btnAddVenue.setOnClickListener {
            findNavController().navigate(R.id.action_add_new_venue,
                Bundle().apply { putString(AddEventFragment.KEY_EVENT_ID, eventId) })
        }
    }

}