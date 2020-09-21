package com.app.meetup.ui.home.models

import com.app.meetup.Profile

data class Vote(
    val placeId: String? = null,
    var count: Int = 0,
    var voters: List<Profile> = mutableListOf()
)