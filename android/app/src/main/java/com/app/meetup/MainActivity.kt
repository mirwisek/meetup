package com.app.meetup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.meetup.repo.Repository
import com.app.meetup.ui.home.isEventOngoing
import com.app.meetup.utils.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.LocalDateTime


class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_PERMISSION_READ_CONTACTS = 1212
        const val RC_PERMISSION_LOCATION = 9091
        const val RC_LOCATION_TURNED_ON = 112
    }

    private lateinit var viewModel: ActivityViewModel
    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FCM requires Google Play services to be installed
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)

        viewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        // If token has been saved in Sharedprefs means we couldn't update it and should do now
        sharedPref.getString(Constants.KEY_FCM_TOKEN, null)?.let { token ->
            getPhoneNoFormatted()?.let { phoneNo ->
                FirestoreUtils.updateFcmToken(phoneNo, token).addOnSuccessListener {
                    // Since we updated token, no need to store it in SharedPrefs
                    sharedPref.edit(true) {
                        remove(Constants.KEY_FCM_TOKEN)
                    }
                }
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS), RC_PERMISSION_READ_CONTACTS
            )

        } else {
            viewModel.loadContacts(getPhoneNoFormatted(), contentResolver, true)
        }

        viewModel.allContacts.observe(this) {
            log("Got contacts ${it.size}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RC_PERMISSION_READ_CONTACTS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast("Can't find your friends, unless you grant permission for contacts")
                } else {
                    viewModel.loadContacts(getPhoneNoFormatted(), contentResolver, true)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        navHostFragment?.let {
            it.childFragmentManager.fragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}