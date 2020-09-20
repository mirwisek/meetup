package com.app.meetup.ui.home

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.app.meetup.Profile
import com.app.meetup.repo.Repository
import com.app.meetup.ui.home.models.Event

class HomeViewModel : ViewModel() {

    val currentLocation = MutableLiveData<Location>()

    private val repo = Repository.getInstance()
    val friends = repo.friends
    val profiles = repo.profiles

    val events = Transformations.map(repo.events) { mList ->
        mList?.let { firestoreEvents ->
            val profileList = profiles.value!!
            firestoreEvents.map { fsEvent ->
                Event(
                    profileList.first { p -> p.phoneNo == fsEvent.organizer!! },
                    fsEvent.startTime!!.toLocalDateTime(),
                    fsEvent.endTime!!.toLocalDateTime(),
                    fsEvent.eventTitle!!,
                    fsEvent.venues.first { v -> fsEvent.selectedVenueId == v.id },
                    fsEvent.venues,
                    profileList.filter { p ->
                        fsEvent.invites.any { inv -> inv == p.phoneNo }
                    },
                    fsEvent.votes,
                    fsEvent.createdAt!!.toLocalDateTime()
                )
            }
        }
    }
}