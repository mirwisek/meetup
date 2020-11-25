package com.app.meetup.repo

import com.app.meetup.Profile
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.ui.home.models.FirestoreVote
import com.app.meetup.ui.home.models.Vote
import com.app.meetup.ui.home.toLocalDateTime
import com.app.meetup.utils.log

/*
 * Mapping FirestoreEvents to Events
 */

object RepoUtils {

    fun transformFirestoreEvents(
        fEvents: MutableList<FirestoreEvent>,
        profileList: MutableList<Profile>
    ): List<Event> {

        val mEvents = fEvents.mapNotNull { fsEvent ->

            val e = Event()

            try {
                e.id = fsEvent.id!!
                e.organizer = profileList.first { p -> p.phoneNo == fsEvent.organizer!! }
                e.startTime = fsEvent.startTime!!.toLocalDateTime()
                e.endTime = fsEvent.endTime!!.toLocalDateTime()

                e.eventTitle = fsEvent.eventTitle!!
                e.selectedVenue = fsEvent.venues.first { v -> fsEvent.selectedVenueId == v.id }
                e.bill = fsEvent.bill!!
                e.createdAt = fsEvent.createdAt!!.toLocalDateTime()

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
            } catch (exc: Exception) {
                log("Skipping event item at RepoUtils")
                exc.printStackTrace()
                null
            }
        }
        return mEvents
    }

    fun toFirestoreVote(votes: MutableList<Vote>): MutableList<FirestoreVote> {
        val list = mutableListOf<FirestoreVote>()

        votes.forEach {  vote ->
            val voters = vote.voters.map { it.phoneNo }
            list.add(FirestoreVote(vote.placeId, voters.toMutableList()))
        }
        return list
    }
}