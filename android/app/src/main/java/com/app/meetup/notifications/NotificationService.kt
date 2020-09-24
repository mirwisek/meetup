package com.app.meetup.notifications

import androidx.core.content.edit
import com.app.meetup.utils.*
import com.google.firebase.messaging.FirebaseMessagingService

class NotificationService: FirebaseMessagingService() {

//    override fun onMessageReceived(msg: RemoteMessage) {
//        super.onMessageReceived(msg)
//    }

    override fun onNewToken(token: String) {

        val phoneNo = getPhoneNoFormatted()

        if(phoneNo == null) {
            // Store in shared preference will be updated to db when phoneNo is available
            storeToDb(token)
        } else {
            FirestoreUtils.updateFcmToken(phoneNo, token).addOnFailureListener { e ->
                storeToDb(token)
                e.printStackTrace()
            }.addOnSuccessListener {
                log("token has been updated")
            }
        }
    }

    private fun storeToDb(token: String) {
        sharedPref.edit {
            putString(Constants.KEY_FCM_TOKEN, token)
        }
    }
}