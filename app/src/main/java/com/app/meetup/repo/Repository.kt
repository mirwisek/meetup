package com.app.meetup.repo

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.meetup.Account
import com.app.meetup.Profile
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.UserData
import com.app.meetup.utils.combineProfilesWithFriends
import com.app.meetup.utils.getPhoneNoFormatted
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Repository private constructor() :CoroutineScope {

    val userData = MutableLiveData<UserData>()
    val profiles = MutableLiveData<MutableList<Profile>>()

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
                        userData.postValue(doc.toObject(UserData::class.java))
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
        FirestoreUtils.getProfiles(phoneNo!!).addSnapshotListener { snap, ex ->
            scope.launch {
                ex?.printStackTrace()

                snap?.let { doc ->
                    try {
                        profiles.postValue(doc.toObjects(Profile::class.java))
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