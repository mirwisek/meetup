package com.app.meetup.ui.home.models

import com.app.meetup.Profile
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import org.threeten.bp.LocalDateTime

data class EventView(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val title: String,
    val locationName: String,
    val address: String,
    val latLng: LatLng,
    val invites: List<Profile>
)

data class Event(
    var organizer: Profile,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    var eventTitle: String,
    var selectedVenue: Venue,
    val venues: List<Venue> = mutableListOf(),
    val invites: List<Profile> = mutableListOf(),
    val votes: List<Vote> = mutableListOf(),
    var createdAt: LocalDateTime
)