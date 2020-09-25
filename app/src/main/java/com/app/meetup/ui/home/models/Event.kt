package com.app.meetup.ui.home.models

import com.app.meetup.Profile
import org.threeten.bp.LocalDateTime

data class Event(
    var id: String,
    var organizer: Profile,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var eventTitle: String,
    var selectedVenue: Venue,
    var bill: String,
    var venues: MutableList<Venue> = mutableListOf(),
    var invites: List<Profile> = mutableListOf(),
    var votes: MutableList<Vote> = mutableListOf(),
    var checkedIn: List<Profile> = mutableListOf(),
    var createdAt: LocalDateTime
)