package com.app.meetup.ui.home.models

import com.app.meetup.Profile
import org.threeten.bp.LocalDateTime

data class Event(
    var id: String = "",
    var organizer: Profile = Profile(),
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime = LocalDateTime.now(),
    var eventTitle: String = "",
    var selectedVenue: Venue = Venue(),
    var bill: String = "0",
    var venues: MutableList<Venue> = mutableListOf(),
    var invites: List<Profile> = mutableListOf(),
    var votes: MutableList<Vote> = mutableListOf(),
    var checkedIn: List<Profile> = mutableListOf(),
    var createdAt: LocalDateTime = LocalDateTime.now()
)