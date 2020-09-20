package com.app.meetup.utils

import com.app.meetup.ui.home.models.FirestoreEvent
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

    fun unFriend(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source,"friends", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(target,"friends", FieldValue.arrayRemove(phoneNo))
            null
        }
    }

    fun addFriend(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source,"requestSent", FieldValue.arrayUnion(targetPhoneNo))
            trans.update(target,"friendRequests", FieldValue.arrayUnion(phoneNo))
            null
        }
    }

    fun cancelPendingRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(source,"requestSent", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(target,"friendRequests", FieldValue.arrayRemove(phoneNo))
            null
        }
    }

    fun acceptFriendRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(target,"requestSent", FieldValue.arrayRemove(phoneNo))
            trans.update(source,"friendRequests", FieldValue.arrayRemove(targetPhoneNo))
            trans.update(source,"friends", FieldValue.arrayUnion(targetPhoneNo))
            trans.update(target,"friends", FieldValue.arrayUnion(phoneNo))
            null
        }
    }

    fun declineFriendRequest(phoneNo: String, targetPhoneNo: String): Task<Task<Void>> {
        val source = getUserData(phoneNo)
        val target = getUserData(targetPhoneNo)

        return db.runTransaction { trans ->
            trans.update(target,"requestSent", FieldValue.arrayRemove(phoneNo))
            trans.update(source,"friendRequests", FieldValue.arrayRemove(targetPhoneNo))
            null
        }
    }

    fun getEvents(): CollectionReference {
        return db.collection("events")
    }

    fun queryEvents(eventsIds: List<String>): Query {
        return db.collection("events")
            .whereIn(FieldPath.documentId(), eventsIds)
    }

    fun addEvent(phoneNo: String, firestoreEvent: FirestoreEvent): Task<Task<Void>> {
        val source = getUserData(phoneNo)

        return db.runTransaction { trans ->

            val eventDoc = trans.get(getEvents().document())
            val id = eventDoc.id

            trans.set(getEvents().document(id), firestoreEvent)

            firestoreEvent.invites.forEach { invitePhone ->
                val target = getUserData(invitePhone)
                trans.update(target, "eventsInvitation", FieldValue.arrayUnion(id))
            }

            trans.update(source, "eventsUpcoming", FieldValue.arrayUnion(id))

            null
        }
    }


}