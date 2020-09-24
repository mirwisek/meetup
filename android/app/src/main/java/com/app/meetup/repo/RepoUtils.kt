package com.app.meetup.repo

import androidx.lifecycle.viewModelScope
import com.app.meetup.Profile
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.ui.home.models.Vote
import com.app.meetup.ui.home.toLocalDateTime
import kotlinx.coroutines.launch

object RepoUtils {

    fun transformFirestoreEvents(
        fEvents: MutableList<FirestoreEvent>,
        profileList: MutableList<Profile>
    ): List<Event> {

        val mEvents = fEvents.map { fsEvent ->
            val e = Event(
                fsEvent.id!!,
                profileList.first { p -> p.phoneNo == fsEvent.organizer!! },
                fsEvent.startTime!!.toLocalDateTime(),
                fsEvent.endTime!!.toLocalDateTime(),
                fsEvent.eventTitle!!,
                fsEvent.venues.first { v -> fsEvent.selectedVenueId == v.id },
                createdAt = fsEvent.createdAt!!.toLocalDateTime()
            )

            e.venues = fsEvent.venues.toMutableList()
            e.invites = profileList.filter { p ->
                fsEvent.invites.any { inv -> inv == p.phoneNo }
            }
            e.votes = fsEvent.votes.map { fv ->
                val eVote = Vote(fv.placeId)

                eVote.voters = profileList.filter { p ->
                    fv.voters.any { v -> v == p.phoneNo }
                }.toMutableSet()

                eVote
            }.toMutableList()

            e.checkedIn = profileList.filter { p ->
                fsEvent.checkedIn.any { c -> c == p.phoneNo }
            }
            e
        }
        return mEvents
    }
}