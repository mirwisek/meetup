package com.app.meetup.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.app.meetup.repo.Repository

class NotificationsViewModel : ViewModel() {

    val repo = Repository.getInstance()

    val notificationList =
        Transformations.map(repo.notifications) { notifications ->
            notifications?.all?.values?.sortedByDescending { it.createdAt?.seconds }
        }

}