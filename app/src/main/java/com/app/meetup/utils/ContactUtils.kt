package com.app.meetup.utils

import android.content.ContentResolver
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts.*
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.asLiveData
import com.app.meetup.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

object ContactUtils {

//    private val selectionArgs = arrayOf(
//        Phone.CONTENT_ITEM_TYPE
//    )

    private val contactsMap = hashMapOf<String, Profile>()



    @ExperimentalCoroutinesApi
    suspend fun fetchContacts(contentResolver: ContentResolver, replaceCountryCode: Boolean) =
        flow<Pair<String, String>> {

            val projection = arrayOf(
                ContactsContract.Data._ID,
                ContactsContract.Data.HAS_PHONE_NUMBER,
                ContactsContract.Data.DISPLAY_NAME_PRIMARY
            )

            val cursor = contentResolver.query(
                CONTENT_URI, projection,
                null, null, null
            )
            if ((cursor?.count ?: 0) > 0) {

                while (cursor != null && cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(_ID))
                    val name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))
                    val hasPhoneNo = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER))

                    if (hasPhoneNo > 0) {
                        emit(Pair(id, name))
                    }
                }
            }
            cursor?.close()

        }
            .transform { contact ->

                val projection = arrayOf(
                    Phone.CONTACT_ID,
                    Phone.NUMBER
                )

                val phoneCursor = contentResolver.query(
                    Phone.CONTENT_URI,
                    projection,
                    "${Phone.CONTACT_ID} = ?",
                    arrayOf(contact.first),
                    null
                )
                while (phoneCursor != null && phoneCursor.moveToNext()) {
                    val no = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER))

                    val phoneNo = PhoneNumberUtils.stripSeparators(no)

                    if (phoneNo.length > 9) {
                        val formattedPhone = if (replaceCountryCode)
                            phoneNo.replace("+92", "0")
                        else
                            phoneNo

//                    contactsMap[formattedPhone] = Profile(contact.second, formattedPhone)
                        emit(Profile(contact.second, formattedPhone))
                    }
                }
                phoneCursor?.close()
            }

//    fun getAllContacts(
//        contentResolver: ContentResolver,
//        replaceCountryCode: Boolean
//    ): HashMap<String, Profile> {
//
//        // using hashmap to avoid duplicate contacts
//        val hashMap = hashMapOf<String, Profile>()
//
//        val cursor = contentResolver.query(
//            CONTENT_URI, null,
//            null, null, null
//        )
//
//        if ((cursor?.count ?: 0) > 0) {
//
//            while (cursor != null && cursor.moveToNext()) {
//                val id = cursor.getString(cursor.getColumnIndex(_ID))
//                val name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))
//
//                val hasPhoneNo = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER))
//
//                if (hasPhoneNo > 0) {
//                    val phoneCursor = contentResolver.query(
//                        Phone.CONTENT_URI,
//                        null,
//                        "${Phone.CONTACT_ID} = ?",
//                        arrayOf(id),
//                        null
//                    )
//                    while (phoneCursor != null && phoneCursor.moveToNext()) {
//                        val no = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER))
//
//                        val phoneNo = PhoneNumberUtils.stripSeparators(no)
//
//                        if (phoneNo.length > 9) {
//                            val formattedPhone = if (replaceCountryCode)
//                                phoneNo.replace("+92", "0")
//                            else
//                                phoneNo
//
//                            hashMap[formattedPhone] = Profile(name, formattedPhone)
//                        }
//                    }
//                    phoneCursor?.close()
//                }
//            }
//        }
//        cursor?.close()
//        return hashMap
//    }
}