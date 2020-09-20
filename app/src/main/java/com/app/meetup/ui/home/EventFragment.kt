package com.app.meetup.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.Invite
import com.app.meetup.R
import com.app.meetup.utils.Constants
import com.app.meetup.utils.gone
import com.app.meetup.utils.visible
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class EventFragment : Fragment() {

    companion object {
        const val TAG = "eventFragment"
    }

    private val bottomSheet = SelectInvitesBottomSheet()
    private var invitesPhoneOnly: ArrayList<String>? = null
    private var eventStartTime: String? = null
    private var eventEndTime: String? = null
    private lateinit var vmBottomSheet: BottomSheetViewModel
    private var isAddressSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_event, container, false)

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

        val timeNow = Calendar.getInstance()
        val hour = timeNow.get(Calendar.HOUR_OF_DAY)
        val min = timeNow.get(Calendar.MINUTE)

        view.btnConfirm.isEnabled = false

        val eventStartTimePicker = TimePickerDialog(
            requireContext(),
            { v, hourOfDay, minute ->
                var p = "AM"
                var finalHour = hourOfDay
                if (hourOfDay > 12) {
                    finalHour = hourOfDay - 12
                    p = "PM"
                }
                eventStartTime = "$finalHour:$minute $p"
                btnStartTime.text = eventStartTime
                enableConfirmIfAllSet()

            }, hour, min, false
        )

        val eventEndTimePicker = TimePickerDialog(
            requireContext(),
            { v, hourOfDay, minute ->
                var p = "AM"
                var finalHour = hourOfDay
                if (hourOfDay > 12) {
                    finalHour = hourOfDay - 12
                    p = "PM"
                }
                eventEndTime = "$finalHour:$minute $p"
                btnEndTime.text = eventEndTime
                enableConfirmIfAllSet()
            }, hour, min, false
        )

        eventEndTimePicker.setTitle("Event End Time")
        eventStartTimePicker.setTitle("Event Starting Time")

        view.btnStartTime.setOnClickListener {
            eventStartTimePicker.show()
        }

        view.btnEndTime.setOnClickListener {
            eventEndTimePicker.show()
        }

        bottomSheet.setOnInviteSelectionListener(object :
            SelectInvitesBottomSheet.OnInviteSelectionListener {

            override fun onComplete(list: List<Invite>) {
                invitesPhoneOnly = ArrayList(list.map { it.profile.phoneNo })
                enableConfirmIfAllSet()
            }
        })

        view.btnConfirm.setOnClickListener {
            val intent = Intent()
            intent.putStringArrayListExtra(Constants.KEY_INVITES_LIST, invitesPhoneOnly)
            requireActivity().apply {
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    fun onPlaceSelected(place: Place) {
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
                eventStartTime != null &&
                eventEndTime != null && isAddressSet -> true
                else -> false
            }
    }

}