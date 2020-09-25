package com.app.meetup.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.app.meetup.ActivityViewModel
import com.app.meetup.MainActivity
import com.app.meetup.R
import com.app.meetup.repo.Repository
import com.app.meetup.ui.home.addevent.MapsActivity
import com.app.meetup.ui.home.eventlist.EditEventFragment
import com.app.meetup.ui.home.eventlist.EventsListRecyclerAdapter
import com.app.meetup.ui.home.models.Event
import com.app.meetup.utils.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var viewModel: ActivityViewModel
    private lateinit var vmHome: HomeViewModel

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)
        vmHome = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        val adapter = EventsListRecyclerAdapter(requireContext())
        view.rvEvents.adapter = adapter

        adapter.setOnItemClickListener(object : EventsListRecyclerAdapter.OnItemClickListener {

            override fun onClicked(event: Event, index: Int) {
                findNavController().navigate(R.id.action_navigation_home_to_editEventFragment,
                    Bundle().apply {
                        putString(EditEventFragment.KEY_EVENT_ID, event.id)
                    })
            }

            override fun onDeleted(event: Event, index: Int) {
                FirestoreUtils.deleteEvent(event).addOnFailureListener {
                    toastFrag("Couldn't delete event, please try again later")
                    it.printStackTrace()
                }.addOnSuccessListener {
                    vmHome.events.value!!.remove(event)
                    Repository.getInstance().updateEventsFromUI(vmHome.events.value!!)
                    toastFrag("Event delete and invited guests notified")
                }
            }

        })

        vmHome.events.observe(viewLifecycleOwner, {
            it?.let { list ->
                view.progressHome.gone()
                if(list.isEmpty()) {
                    view.placeHolder.text = getString(R.string.no_events)
                    view.placeHolder.visible()
                } else {
                    view.placeHolder.gone()
                    adapter.updateList(list)
                }
            }
        })

        vmHome.errors.observe(viewLifecycleOwner) {
            it?.let { map ->
                log("maps $map")
                map[Constants.EVENTS]?.let { value ->
                    if(value) {
                        view.placeHolder.text = getString(R.string.error_events)
                        view.placeHolder.visible()
                        view.placeHolder.gone()
                    }
                }
            }
        }

        view.fabAdd.setOnClickListener {
            if (hasLocationPermission()) {
                enableGPS()
            } else {
                requestPermissions(locationPermissions, MainActivity.RC_PERMISSION_LOCATION)
            }
        }

    }

    private fun hasLocationPermission(): Boolean {
        val context = requireContext()

        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }


    private fun enableGPS() {

        val task = LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest()).build()
        )

        task.addOnCompleteListener {
            try {
                if (it.isSuccessful)
                    onFabClicked()
                it.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                try {
                    // Cast to a resolvable exception.
                    val resolvable = e as ResolvableApiException
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    resolvable.startResolutionForResult(
                        requireActivity(),
                        MainActivity.RC_LOCATION_TURNED_ON
                    )
                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                } catch (e: ClassCastException) {
                    // Ignore, should be an impossible error.
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MainActivity.RC_PERMISSION_LOCATION -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toastFrag("Can't access maps unless you grant location permission")
                } else {
                    enableGPS()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            MainActivity.RC_LOCATION_TURNED_ON -> {
                if (resultCode == Activity.RESULT_OK)
                    enableGPS()
            }
        }
    }

    private fun onFabClicked() {

        startActivity(
            Intent(requireActivity(), MapsActivity::class.java)
        )
        // Reset value from EditEvents
        vmHome.currentEventTitle.postValue(null)
    }

}