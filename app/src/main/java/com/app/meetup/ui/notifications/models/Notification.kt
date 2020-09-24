package com.app.meetup.ui.notifications.models

import com.google.firebase.Timestamp

data class Notification (
    val title: String? = null,
    val body: String? = null,
    val isSeen: Boolean? = null,
    val createdAt: Timestamp? = null
)

data class NotificationsList (
    val all: HashMap<String, Notification> = hashMapOf()
)
