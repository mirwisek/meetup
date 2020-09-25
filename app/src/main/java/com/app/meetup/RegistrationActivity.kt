package com.app.meetup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.app.meetup.ui.notifications.models.Notification
import com.app.meetup.utils.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)


        btnGetStarted.setOnClickListener {
            val name = textName.text.toString()

            if(name.isNotEmpty()) {
                progress.visible()
                val user = FirebaseAuth.getInstance().currentUser!!
                // Don't want international code, as contacts no are usually starting with '0'
                val phone = user.phoneNumber!!.replace("+92", "0")

                // Need a dummy notification to create a document, so we can update notifications
                // without document exist exception
                val notification = Notification("Welcome to Meeup",
                    "You have created meetup account", false, Timestamp.now())

                FirestoreUtils.createUser(Profile(name, phone), notification).addOnSuccessListener {
                    progress.gone()
                    sharedPref.edit {
                        putBoolean(Constants.KEY_REGISTRATION_COMPLETE, true)
                    }
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    toast("Couldn't create your account, please try again later")
                    it.printStackTrace()
                }
            } else {
                toast("User name can't be empty")
            }
        }

    }
}