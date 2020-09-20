package com.app.meetup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.utils.gone
import com.app.meetup.utils.toast
import com.app.meetup.utils.visible
import com.google.firebase.auth.FirebaseAuth
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

                FirestoreUtils.getProfile(phone).set(
                    Profile(name, phone)
                ).continueWith {
                    // Create user data document as well
                    FirestoreUtils.getUserData(phone).set(UserData())

                }.addOnSuccessListener {
                    progress.gone()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            } else {
                toast("User name can't be empty")
            }
        }

    }
}