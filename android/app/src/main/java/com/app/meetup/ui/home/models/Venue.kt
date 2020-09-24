package com.app.meetup.ui.home.models

import com.google.firebase.firestore.GeoPoint

data class Venue(
    val id: String? = null,
    val addedBy: String? = null,
    val locationName: String? = null,
    val address: String? = null,
    val latLng: GeoPoint? = null
)