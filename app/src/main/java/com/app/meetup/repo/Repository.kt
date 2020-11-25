package com.app.meetup.repo

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.meetup.Account
import com.app.meetup.Profile
import com.app.meetup.UserData
import com.app.meetup.ui.home.models.Event
import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.ui.home.models.InviteMessage
import com.app.meetup.ui.notifications.models.Notification
import com.app.meetup.ui.notifications.models.NotificationsList
import com.app.meetup.utils.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Repository private constructor() : CoroutineScope {

    val inviteTemplate = MutableLiveData<InviteMessage>()
    val userData = MutableLiveData<UserData>()
    val profiles = MutableLiveData<MutableList<Profile>>()
    val currentProfile = MutableLiveData<Profile>()
    val events = MutableLiveData<Result<MutableList<Event>>>()
    val notifications = MutableLiveData<NotificationsList>()

    var errors = MutableLiveData<HashMap<String, Boolean>>().apply {
        postValue(hashMapOf())
    }

    var empty = MutableLiveData<HashMap<String, Boolean>>().apply {
        postValue(hashMapOf())
    }


    val friends = userData.combineProfilesWithFriends(profiles) { profilesList, userData ->

        val friendsList = mutableListOf<Profile>()
        profilesList.forEach { profile ->
            if (userData.friends.any { friend -> friend == profile.phoneNo })
                friendsList.add(profile)
        }
        friendsList
    }

    private var phoneNo: String? = null

    init {
        this.phoneNo = getPhoneNoFormatted()
        fetchData()
        fetchProfiles()
        fetchNotifications()
        fetchInviteTemplate()
    }

    companion object {
        private var instance: Repository? = null

        fun getInstance(): Repository {

            if (instance == null)
                instance = Repository()
            return instance!!
        }

        // When user provides permissions then fetch data again
        fun createNewInstance(): Repository {
            instance = Repository()
            return instance!!
        }
    }

    fun updateEventsFromUI(e: MutableList<Event>) {
        events.postValue(Result.success(e))
    }

    private fun fetchData() {
        if (phoneNo == null)
            return

        FirestoreUtils.getUserData(phoneNo!!).addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.let { e ->
                    events.postValue(Result.failure(e))
                }

                snap?.let { doc ->

                    try {
                        val data = doc.toObject(UserData::class.java)
                        userData.postValue(data)

                        // Only Retrieve the required events from Fb, not all

                        if(data == null) {
                            empty.value!![Constants.USERDATA] = true
                            events.postValue(Result.success(mutableListOf()))
                        } else
                            fetchEvents(data)
                    } catch (e: Exception) {
                        events.postValue(Result.failure(e))
                    }
                }
            }
        }
    }

    private fun fetchInviteTemplate() {
        FirestoreUtils.getInviteTemplate().get().addOnSuccessListener { snap ->
            scope.launch {
                if(snap != null) {
                    val message = snap.toObject(InviteMessage::class.java)
                    inviteTemplate.postValue(message)
                }
            }
        }.addOnFailureListener {
            inviteTemplate.postValue(InviteMessage("https://www.google.com", "You have been invited to Meetup Android"))
        }
    }

    private fun fetchProfiles() {
        if (phoneNo == null)
            return
        FirestoreUtils.getProfiles().addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.let {
                    it.printStackTrace()
                    log("Profile exceptio n ${it.message}")
                    errors.value!![Constants.PROFILES] = true
                }

                snap?.let { doc ->
                    try {
                        val _profiles = doc.toObjects(Profile::class.java)
                        profiles.postValue(_profiles)
                        val cProfile = _profiles.firstOrNull { p ->
                            p.phoneNo == phoneNo
                        }
                        log("Fetched profile from server $cProfile")
                        currentProfile.postValue(cProfile)
                    } catch (e: Exception) {
                        errors.value!![Constants.PROFILES] = true
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun fetchEvents(data: UserData) {
        if (phoneNo == null)
            return

        // We are calling this function when userData has been retrieved
        val mergedList = data.let { mData ->
            mData.eventsInvitation + mData.eventsOnGoing + mData.eventsUpcoming
        }

        if(mergedList.isEmpty()) {
            events.postValue(Result.success(mutableListOf()))
            return
        }
        // Woulnd't update items instantly
//        FirestoreUtils.queryEvents(mergedList).addOnSuccessListener { snaps ->
//            scope.launch {
//
//                try {
//                    var firestoreEvents = listOf<FirestoreEvent>()
//                    snaps?.forEach { doc ->
//                        firestoreEvents =
//                            firestoreEvents + doc.toObjects(FirestoreEvent::class.java)
//                    }
//                    val newEvents = RepoUtils.transformFirestoreEvents(
//                        firestoreEvents.toMutableList(), profiles.value!!
//                    )
//                    log("New events ${newEvents}")
//                    events.postValue(newEvents.toMutableList())
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }.addOnFailureListener {
//            it.printStackTrace()
//            errors.value!![Constants.EVENTS] = true
//            events.postValue(mutableListOf())
//        }

        FirestoreUtils.getEvents().addSnapshotListener { snaps, ex ->
            scope.launch {

                try {

                    ex?.let { e ->
                        events.postValue(Result.failure(e))
                    }

                    var firestoreEvents = mutableListOf<FirestoreEvent>()
                    snaps?.forEach { doc ->

                        mergedList.forEach { q ->
                            if(doc.id == q) {
                                firestoreEvents.add(doc.toObject(FirestoreEvent::class.java))
                            }
                        }

                    }
                    val newEvents = RepoUtils.transformFirestoreEvents(
                        firestoreEvents, profiles.value!!
                    )
                    log("New events ${newEvents}")
                    events.postValue(Result.success(newEvents.toMutableList()))

                } catch (e: Exception) {
                    events.postValue(Result.failure(e))
                }
            }
        }
    }

    fun fetchNotifications() {
        if (phoneNo == null) {
            notifications.postValue(NotificationsList())
            return
        }

        FirestoreUtils.getNotifications(phoneNo!!).addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.let {
                    it.printStackTrace()
                    errors.value!![Constants.NOTIFICATION] = true
                }

                val map = NotificationsList(hashMapOf())

                snap?.let { doc ->
                    try {
                        doc.data?.forEach { (k,v) ->
                            val value = v as Map<*, *>
                            val notif = Notification(
                                value["title"] as String,
                                value["body"] as String,
                                value["isSeen"] as Boolean,
                                value["createdAt"] as Timestamp
                            )
                            map.all[k as String] = notif
                        }
                        notifications.postValue(map)
                    } catch (e: Exception) {
                        log("Notifications exception")
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