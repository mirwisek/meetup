package com.app.meetup.utils

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.app.meetup.Account
import com.app.meetup.Profile
import com.app.meetup.R
import com.app.meetup.UserData
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

fun Context.showSnackbar(
    view: View?,
    text: String, length: Int = Snackbar.LENGTH_SHORT,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
//                       color: Int = R.color.logoBg
) {
    // Backoff to toast if view is not available
    if (view == null) {
        toast(text)
    } else {
        val snackbar =
            Snackbar.make(view, text, length)
                .setAnimationMode(animationMode)
//        snackbar.view.setBackgroundColor(view.context.getColorCompat(color))
        snackbar.show()
    }
}

fun Fragment.toastFrag(msg: String, len: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(msg, len)
}

fun Context.toast(msg: String, len: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, len).show()
}

fun log(msg: String) {
    Log.d("🌀❤💕😎🤷 ffnet :: ‍", msg)
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

internal fun View.setBackgroundTintCompat(@ColorRes color: Int) = ViewCompat.setBackgroundTintList(
    this,
    ContextCompat.getColorStateList(context, color)
)

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.getDrawableCompat(@DrawableRes drawable: Int) = ContextCompat.getDrawable(this, drawable)


fun Context.toPx(dp: Int): Int {
    return (dp * (resources.displayMetrics.densityDpi.toFloat()
            / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Context.toDp(px: Int): Int {
    return (px / (resources.displayMetrics.densityDpi.toFloat()
            / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun LiveData<MutableList<Profile>>.combineContacts(
    contacts: LiveData<HashMap<String, Profile>>,
    callback: (HashMap<String, Profile>, MutableList<Profile>) -> MutableList<Account>?
): LiveData<MutableList<Account>?> {

    val result = MediatorLiveData<MutableList<Account>?>()

    // Will call only when both are not null and set
    result.addSource(this) {
        it?.let { profiles ->
            contacts.value?.let { contactList ->
                result.value =
                    callback.invoke(contactList, profiles)
            }
        }
    }
    result.addSource(contacts) {
        it?.let { contactList ->
            value?.let { profiles ->
                result.value = callback.invoke(contactList, profiles)
            }
        }
    }
    return result
}

fun LiveData<UserData>.combineProfilesWithFriends(
    profiles: LiveData<MutableList<Profile>>,
    callback: (MutableList<Profile>, UserData) -> MutableList<Profile>
): LiveData<MutableList<Profile>> {

    val result = MediatorLiveData<MutableList<Profile>>()

    // Will call only when both are not null and set
    result.addSource(this) {
        it?.let { userData ->
            profiles.value?.let { profilesList ->
                result.value =
                    callback.invoke(profilesList, userData)
            }
        }
    }
    result.addSource(profiles) {
        it?.let { profilesList ->
            value?.let { userData ->
                result.value = callback.invoke(profilesList, userData)
            }
        }
    }
    return result
}

fun getPhoneNoFormatted(): String? {
    return FirebaseAuth.getInstance()
        .currentUser?.phoneNumber?.replace("+92", "0")
}

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun Location.toText(): String {
    return "$latitude,$longitude"
}

fun String.toLatLng(): LatLng {
    val item = this.split(",")
    return LatLng(
        item[0].toDouble(),
        item[1].toDouble()
    )
}

val Context.sharedPref: SharedPreferences
    get() = getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE)

fun getLocationRequest(): LocationRequest {
    return LocationRequest.create().apply {
        interval = 5_000
        fastestInterval = 3000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}
