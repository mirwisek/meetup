package com.app.meetup

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.*
import android.provider.ContactsContract.Contacts.*

object ContactUtils {

    fun getAllContacts(contentResolver: ContentResolver, replaceCountryCode: Boolean): HashMap<String, Contact> {

        // using hashmap to avoid duplicate contacts
        val hashMap = hashMapOf<String, Contact>()

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
                        val phoneNo = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER))

                        val formattedPhone = if(replaceCountryCode)
                            phoneNo.replace("+92", "0")
                        else
                            phoneNo

                        hashMap[formattedPhone] = Contact(name, formattedPhone)
                    }
                    phoneCursor?.close()
                }
            }
        }
        cursor?.close()
        return hashMap
    }
}