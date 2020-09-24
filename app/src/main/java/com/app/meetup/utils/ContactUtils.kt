package com.app.meetup.utils

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts.*
import android.telephony.PhoneNumberUtils
import com.app.meetup.Profile

object ContactUtils {

    fun getAllContacts(contentResolver: ContentResolver, replaceCountryCode: Boolean): HashMap<String, Profile> {

        // using hashmap to avoid duplicate contacts
        val hashMap = hashMapOf<String, Profile>()

        val cursor = contentResolver.query(CONTENT_URI, null,
        null, null, null)

        if((cursor?.count ?: 0) > 0) {
            while(cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(_ID))
                val name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))

                val hasPhoneNo = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER))

                if(hasPhoneNo > 0) {
                    val phoneCursor = contentResolver.query(
                        Phone.CONTENT_URI,
                        null,
                        "${Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    while(phoneCursor != null && phoneCursor.moveToNext()) {
                        val no = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER))

                        val phoneNo = PhoneNumberUtils.stripSeparators(no)

                        if(phoneNo.length > 9) {
                            val formattedPhone = if(replaceCountryCode)
                                phoneNo.replace("+92", "0")
                            else
                                phoneNo

                            hashMap[formattedPhone] = Profile(name, formattedPhone)
                        }
                    }
                    phoneCursor?.close()
                }
            }
        }
        cursor?.close()
        return hashMap
    }
}