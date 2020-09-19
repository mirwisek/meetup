package com.app.meetup

import android.content.Context
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

fun LiveData<UserData>.combineContacts(
    contacts: LiveData<MutableList<Account>>,
    callback: (MutableList<Account>, UserData) -> MutableList<Account>
): LiveData<MutableList<Account>> {

    val result = MediatorLiveData<MutableList<Account>>()

    // Will call only when both are not null and set
    result.addSource(this) {
        it?.let { userData ->
            contacts.value?.let { contactList ->
                result.value =
                    callback.invoke(contactList, userData)
            }
        }
    }
    result.addSource(contacts) {
        it?.let { contactList ->
            value?.let { userData ->
                result.value = callback.invoke(contactList, userData)
            }
        }
    }
    return result
}

fun getPhoneNoFormatted(): String {
    return FirebaseAuth.getInstance()
        .currentUser!!.phoneNumber!!.replace("+92", "0")
}
