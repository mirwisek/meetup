package com.app.meetup.ui.home.models

import com.google.firebase.Timestamp

data class FirestoreEvent(
    var organizer: String? = null,
    var startTime: Timestamp? = null,
    var endTime: Timestamp? = null,
    var eventTitle: String? = null,
    var selectedVenueId: String? = null,
    var venues: List<Venue> = mutableListOf(),
    var invites: List<String> = mutableListOf(),
    var votes: List<Vote> = mutableListOf(),
    var createdAt: Timestamp? = null
)