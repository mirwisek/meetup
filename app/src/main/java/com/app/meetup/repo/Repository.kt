package com.app.meetup.repo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.meetup.FirestoreUtils
import com.app.meetup.UserData
import com.app.meetup.getPhoneNoFormatted
import com.app.meetup.log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Repository private constructor() :CoroutineScope {

    val userData = MutableLiveData<UserData>()
    private lateinit var application: Application
    private lateinit var phoneNo: String

    companion object {
        private var instance: Repository? = null

        fun getInstance(application: Application): Repository {

            if (instance == null)
                instance = Repository(application)
            return instance!!
        }
    }

    constructor(application: Application) : this() {
        this.application = application
        this.phoneNo = getPhoneNoFormatted()

        fetchData()
    }

    fun fetchData() {
        FirestoreUtils.getUserData(phoneNo).addSnapshotListener { snap, ex ->
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

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)
}