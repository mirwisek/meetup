package com.app.meetup.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.meetup.repo.Repository

class HomeViewModel : ViewModel() {

    val currentLocation = MutableLiveData<Location>()

    private val repo = Repository.getInstance()
    val friends = repo.friends
}