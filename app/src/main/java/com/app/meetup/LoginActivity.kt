package com.app.meetup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.meetup.repo.Repository
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.utils.getPhoneNoFormatted
import com.app.meetup.utils.log
import com.app.meetup.utils.showSnackbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    companion object {
        const val RC_SIGN_IN = 10210
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build()
        )

        // Create and launch sign-in intent
        btnLogin.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK && response != null) {
                // Successfully signed in
//                if(response.isNewUser) {
                    FirestoreUtils.getUserData(getPhoneNoFormatted()!!).get().addOnSuccessListener {
                        if(it.contains("friends")) {
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            startActivity(Intent(this, RegistrationActivity::class.java))
                        }
                    }.addOnFailureListener {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
//                } else {
//                    startActivity(Intent(this, MainActivity::class.java))
//                }
                Repository.createNewInstance()
                finish()

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button.
                if (response == null) {
                    showError("User cancelled login!")
                } else {
                    showError("An error occured during login: ${response.error?.errorCode}")
                    log("Login Error ${response.error?.message}")
                    response.error?.printStackTrace()
                }
            }
        }
    }

    private fun showError(msg: String) {
        showSnackbar(container, msg)
    }
}