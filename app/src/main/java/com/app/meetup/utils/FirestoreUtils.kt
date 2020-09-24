package com.app.meetup.utils

import com.app.meetup.ui.home.models.FirestoreEvent
import com.app.meetup.ui.home.models.FirestoreVote
import com.app.meetup.ui.home.models.Venue
import com.app.meetup.ui.home.models.Vote
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.min

object FirestoreUtils {

    private val db = Firebase.firestore

    init {
        val settings = FirebaseFirestoreSettings.Builder()
        settings.isPersistenceEnabled = false
        db.firestoreSettings = settings.build()
    }

    fun getProfile(phoneNo: String): DocumentReference {
        return db.collection("profiles").document(phoneNo)
    }

    fun getUserData(phoneNo: String): DocumentReference {
        return db.collection("users").document(phoneNo)
    }

    fun getProfiles(): CollectionReference {
        return db.collection("profiles")
    }

    fun queryProfiles(phoneList: List<String>): Task<MutableList<QuerySnapshot>>  {
        val queryList = mutableListOf<Task<QuerySnapshot>>()

        if(phoneList.size < 10) {
            queryList.add(getProfiles()
                .whereIn(FieldPath.documentId(), phoneList).get()
            )
        }
        // There is a limit on single query "whereIn" of 10, so we divide eventsIds in batches
        // of 9
        else {
            val loopCount = phoneList.size / 9

            for(i in 0..loopCount) {
                val start = i*9
                val end = if(i == loopCount) start + (phoneList.size % start) else start + 9
                // For cases where list size is divisible by 9
                if(start != end) {
                    queryList.add(
                        getProfiles()
                            .whereIn(FieldPath.documentId(), phoneList.subList(start, end)).get()
                    )
                }
            }
        }

        return Tasks.whenAllSuccess(queryList)
    }

    fun unFriend(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source, "friends", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(target, "friends", FieldValue.arrayRemove(phoneNo))
            null
        }
    }

    fun addFriend(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source, "requestSent", FieldValue.arrayUnion(targetPhoneNo))
            trans.update(target, "friendRequests", FieldValue.arrayUnion(phoneNo))
            null
        }
    }

    fun cancelPendingRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source, "requestSent", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(target, "friendRequests", FieldValue.arrayRemove(phoneNo))
            null
        }
    }

    fun acceptFriendRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(target, "requestSent", FieldValue.arrayRemove(phoneNo))
            trans.update(source, "friendRequests", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(source, "friends", FieldValue.arrayUnion(targetPhoneNo))
            trans.update(target, "friends", FieldValue.arrayUnion(phoneNo))
            null
        }
    }

    fun declineFriendRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(target, "requestSent", FieldValue.arrayRemove(phoneNo))
            trans.update(source, "friendRequests", FieldValue.arrayRemove(targetPhoneNo))
            null
        }
    }

    fun getEvents(): CollectionReference {
        return db.collection("events")
    }

    fun queryEvents(eventsIds: List<String>): Task<MutableList<QuerySnapshot>> {

        val queryList = mutableListOf<Task<QuerySnapshot>>()

        if(eventsIds.size < 10) {
            queryList.add(db.collection("events")
                .whereIn(FieldPath.documentId(), eventsIds).get()
            )
        }
        // There is a limit on single query "whereIn" of 10, so we divide eventsIds in batches
        // of 9
        else {
            val loopCount = eventsIds.size / 9

            for(i in 0..loopCount) {
                val start = i*9
                val end = if(i == loopCount) start + (eventsIds.size % start) else start + 9
                // For cases where list size is divisible by 9
                if(start != end) {
                    queryList.add(
                        db.collection("events")
                            .whereIn(FieldPath.documentId(), eventsIds.subList(start, end)).get()
                    )
                }
            }
        }

        return Tasks.whenAllSuccess(queryList)
    }

    fun addEvent(phoneNo: String, firestoreEvent: FirestoreEvent): Task<Task<Void>> {
        val source = getUserData(phoneNo)

        return db.runTransaction { trans ->

            val eventDoc = trans.get(getEvents().document())
            val id = eventDoc.id

            firestoreEvent.id = id
            trans.set(getEvents().document(id), firestoreEvent)

            firestoreEvent.invites.forEach { invitePhone ->
                val target = getUserData(invitePhone)
                trans.update(target, "eventsInvitation", FieldValue.arrayUnion(id))
            }

            trans.update(source, "eventsUpcoming", FieldValue.arrayUnion(id))

            null
        }
    }

    fun updateVenue(eventId: String, phoneNo: String, venues: List<Venue>, selectedVenue: String,
        votes: List<FirestoreVote>
    ): Task<Task<Void>> {

        val source = getUserData(phoneNo)

        return db.runTransaction { trans ->

            trans.update(
                getEvents().document(eventId),
                hashMapOf(
                    "selectedVenueId" to selectedVenue,
                    "venues" to venues,
                    "votes" to votes
                )
            )

            trans.update(source, "eventsInvitation", FieldValue.arrayRemove(eventId))
            trans.update(source, "eventsUpcoming", FieldValue.arrayUnion(eventId))

            null
        }
    }

    fun updateTitle(eventId: String, title: String): Task<Void> {
        return getEvents().document(eventId).update(
            hashMapOf(
                "eventTitle" to title
            ) as Map<String, Any>
        )
    }

    fun checkIn(eventId: String, phoneNo: String): Task<Void> {
        return getEvents().document(eventId)
            .update("checkedIn", FieldValue.arrayUnion(phoneNo))

    }

    fun updateFcmToken(phoneNo: String, token: String): Task<Void> {
        return db.document("tokens/fcm")
            .update(phoneNo, token)
    }

}