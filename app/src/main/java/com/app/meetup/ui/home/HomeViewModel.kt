package com.app.meetup.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.meetup.Profile
import com.app.meetup.repo.Repository
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.Vote
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = Repository.getInstance()
    val friends = repo.friends
    val profiles = repo.profiles

    val currentProfile = repo.currentProfile

    val events = repo.events

    val currentEventTitle = MutableLiveData<String>()

}