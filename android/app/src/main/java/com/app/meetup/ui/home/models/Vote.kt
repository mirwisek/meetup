package com.app.meetup.ui.home.models

import com.app.meetup.Profile

data class Vote(
    val placeId: String? = null,
    var voters: MutableSet<Profile> = mutableSetOf()
)