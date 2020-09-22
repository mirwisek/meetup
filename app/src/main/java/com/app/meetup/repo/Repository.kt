package com.app.meetup.repo

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.meetup.Account
import com.app.meetup.Profile
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.UserData
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.utils.combineProfilesWithFriends
import com.app.meetup.utils.getPhoneNoFormatted
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Repository private constructor() :CoroutineScope {

    val userData = MutableLiveData<UserData>()
    val profiles = MutableLiveData<MutableList<Profile>>()
    val currentProfile = MutableLiveData<Profile>()
    val events = MutableLiveData<MutableList<Event>>()

    val friends = userData.combineProfilesWithFriends(profiles) { profilesList, userData ->

        val friendsList = mutableListOf<Profile>()
        profilesList.forEach { profile ->
            if(userData.friends.any { friend -> friend == profile.phoneNo })
                friendsList.add(profile)
        }
        friendsList
    }

    private var phoneNo: String? = null

    init {
        this.phoneNo = getPhoneNoFormatted()
        fetchData()
        fetchProfiles()
    }

    companion object {
        private var instance: Repository? = null

        fun getInstance(): Repository {

            if (instance == null)
                instance = Repository()
            return instance!!
        }
    }

    private fun fetchData() {
        if(phoneNo == null)
            return
        FirestoreUtils.getUserData(phoneNo!!).addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.printStackTrace()

                snap?.let { doc ->
                    try {
                        val data = doc.toObject(UserData::class.java)
                        userData.postValue(data)
                        // Only Retrieve the required events from Fb, not all
                        data?.let { fetchEvents(it) }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun fetchProfiles() {
        if(phoneNo == null)
            return
        FirestoreUtils.getProfiles().addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.printStackTrace()

                snap?.let { doc ->
                    try {
                        val _profiles = doc.toObjects(Profile::class.java)
                        profiles.postValue(_profiles)
                        val cProfile = _profiles.first { p ->
                            p.phoneNo == phoneNo
                        }
                        currentProfile.postValue(cProfile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun fetchEvents(data: UserData) {
        if(phoneNo == null)
            return

        // We are calling this function when userData has been retrieved
        val mergedList = data.let { mData ->
            mData.eventsInvitation + mData.eventsOnGoing + mData.eventsUpcoming
        }
        FirestoreUtils.queryEvents(mergedList).addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.printStackTrace()

                snap?.let { doc ->
                    try {
                        val firestoreEvents = doc.toObjects(FirestoreEvent::class.java)
                        val newEvents = RepoUtils.transformFirestoreEvents(
                            firestoreEvents, profiles.value!!
                        )
                        events.postValue(newEvents.toMutableList())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

}