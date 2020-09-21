package com.app.meetup.ui.home.models

import com.app.meetup.Profile
import org.threeten.bp.LocalDateTime

data class Event(
    var id: String,
    var organizer: Profile,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    var eventTitle: String,
    var selectedVenue: Venue,
    val venues: List<Venue> = mutableListOf(),
    val invites: List<Profile> = mutableListOf(),
    val votes: List<Vote> = mutableListOf(),
    val checkedIn: List<Profile> = mutableListOf(),
    var createdAt: LocalDateTime
)