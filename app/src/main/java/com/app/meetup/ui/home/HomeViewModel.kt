package com.app.meetup.ui.home

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.meetup.Profile
import com.app.meetup.repo.Repository
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.Vote
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = Repository.getInstance()
    val friends = repo.friends
    val profiles = repo.profiles

    val firestoreEvents = repo.events

    val events = MutableLiveData<List<Event>>()

    fun refactorEvents() {
        viewModelScope.launch {
            firestoreEvents.value?.let { fEvents ->

                val profileList = profiles.value!!

                val mEvents = fEvents.map { fsEvent ->

                    Event(
                        fsEvent.id!!,
                        profileList.first { p -> p.phoneNo == fsEvent.organizer!! },
                        fsEvent.startTime!!.toLocalDateTime(),
                        fsEvent.endTime!!.toLocalDateTime(),
                        fsEvent.eventTitle!!,
                        fsEvent.venues.first { v -> fsEvent.selectedVenueId == v.id },
                        fsEvent.venues,
                        profileList.filter { p ->
                            fsEvent.invites.any { inv -> inv == p.phoneNo }
                        },
                        fsEvent.votes.map { fv ->
                            val eVote = Vote(fv.placeId, fv.count)
                            eVote.voters = profileList.filter { p ->
                                fv.voters.any { v -> v == p.phoneNo }
                            }
                            eVote
                        },
                        profileList.filter { p ->
                            fsEvent.checkedIn.any { c -> c == p.phoneNo }
                        },
                        fsEvent.createdAt!!.toLocalDateTime()
                    )
                }
                events.postValue(mEvents)
            }
        } // viewmodelscope end
    }
}