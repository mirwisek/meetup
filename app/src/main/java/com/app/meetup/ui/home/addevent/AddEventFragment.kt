package com.app.meetup.ui.home.addevent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.Invite
import com.app.meetup.R
import com.app.meetup.ui.home.getFormatted
import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.ui.home.models.FirestoreVote
import com.app.meetup.ui.home.models.Venue
import com.app.meetup.ui.home.toGeoPoint
import com.app.meetup.ui.home.toTimestamp
import com.app.meetup.utils.*
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_add_event.*
import kotlinx.android.synthetic.main.fragment_add_event.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class AddEventFragment : Fragment() {

    companion object {
        const val TAG = "eventFragment"
    }

    private val bottomSheet = SelectInvitesBottomSheet()
    private lateinit var vmBottomSheet: BottomSheetViewModel


    private var invitesPhoneOnly: List<String> = mutableListOf()
    private var showStartTimeAfterDate = -1

    private var eventDate: LocalDate? = null

    private var eventStart: LocalDateTime? = null
    private var eventEnd: LocalDateTime? = null

    private var venue: Venue? = null

    private var isAddressSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_add_event, container, false)

        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vmBottomSheet = ViewModelProvider(requireActivity()).get(BottomSheetViewModel::class.java)

        view.btnInvitePeople.setOnClickListener {
            bottomSheet.show(parentFragmentManager, SelectInvitesBottomSheet::class.simpleName)
        }

        vmBottomSheet.friends.observe(viewLifecycleOwner, { list ->
            list?.let { profiles ->
                val invites = profiles.map { Invite(it, false) }
                vmBottomSheet.inviteList.postValue(invites)
                vmBottomSheet.friends.removeObservers(viewLifecycleOwner)
            }
        })

        vmBottomSheet.selectedInvites.observe(viewLifecycleOwner, { list ->
            if (list.isNullOrEmpty()) {
                view.btnInvitePeople.text = "INVITED CONTACTS (0)"
            } else {
                view.btnInvitePeople.text = "INVITED CONTACTS (${list.size})"
            }
        })

        view.btnConfirm.isEnabled = false

        val today = LocalDateTime.now()


        val eventStartTimePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val eventStartTime = LocalTime.of(hourOfDay, minute)
                eventStart = LocalDateTime.of(eventDate!!, eventStartTime)
                btnStartTime.text = eventStart!!.getFormatted()
                enableConfirmIfAllSet()

            }, today.hour, today.minute, false
        )

        val eventEndTimePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val eventEndTime = LocalTime.of(hourOfDay, minute)
                eventEnd = LocalDateTime.of(eventDate!!, eventEndTime)
                btnEndTime.text = eventEnd!!.getFormatted()
                enableConfirmIfAllSet()
            }, today.hour, today.minute, false
        )

        val startDatePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            eventDate = LocalDate.of(year, month, dayOfMonth)
            // Now we track which button triggered datePicker 1=StartTime and 0=EndTime
            if(showStartTimeAfterDate == 1)
                eventStartTimePicker.show()
            else if(showStartTimeAfterDate == 0)
                eventEndTimePicker.show()
        }, today.year, today.monthValue, today.dayOfMonth)


        startDatePicker.setTitle("Event Date")
        eventEndTimePicker.setTitle("Event End Time")
        eventStartTimePicker.setTitle("Event Starting Time")

        view.btnStartTime.setOnClickListener {
            if(eventDate == null) {
                // 1 means true but -1 means unset
                showStartTimeAfterDate = 1
                startDatePicker.show()
            } else
                eventStartTimePicker.show()
        }

        view.btnEndTime.setOnClickListener {
            if(eventDate == null) {
                // 0 means false
                showStartTimeAfterDate = 0
                startDatePicker.show()
            } else
                eventEndTimePicker.show()
        }

        bottomSheet.setOnInviteSelectionListener(object :
            SelectInvitesBottomSheet.OnInviteSelectionListener {

            override fun onComplete(list: List<Invite>) {
                invitesPhoneOnly = list.map { it.profile.phoneNo }
                enableConfirmIfAllSet()
            }
        })

        view.btnConfirm.setOnClickListener {
            val intent = Intent()
            val phone = getPhoneNoFormatted()!!
            val event = FirestoreEvent(
                null,
                phone,
                eventStart!!.toTimestamp(),
                eventEnd!!.toTimestamp(),
                "Get together with friends",
                venue!!.id,
                mutableListOf(venue!!),
                invitesPhoneOnly,
                mutableListOf(FirestoreVote(venue!!.id, 1, listOf(phone))),
                mutableListOf(),
                Timestamp.now()
            )

            FirestoreUtils.addEvent(getPhoneNoFormatted()!!, event).addOnFailureListener {
                toastFrag("Couldn't post your event, server error!")
                it.printStackTrace()
            }.addOnSuccessListener {
                requireActivity().apply {
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    fun onPlaceSelected(place: Place) {

        venue = Venue(place.id!!, place.name!!, place.address!!, place.latLng!!.toGeoPoint())

        locationName.text = place.name
        address.text = place.address
        locationName.visible()
        address.visible()
        hint.gone()
        isAddressSet = true
        enableConfirmIfAllSet()
    }

    private fun enableConfirmIfAllSet() {
        btnConfirm.isEnabled =
            when {
                !invitesPhoneOnly.isNullOrEmpty() &&
                eventStart != null &&
                eventEnd != null && isAddressSet -> true
                else -> false
            }
    }

}