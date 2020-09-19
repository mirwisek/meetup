package com.app.meetup.ui.friends

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.meetup.*
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {

    private val _contactsList = MutableLiveData<HashMap<String, Contact>>()
    val contactsList: LiveData<HashMap<String, Contact>> = _contactsList

    private val _phoneNoList = MutableLiveData<List<String>>()
    val phoneNoList: LiveData<List<String>> = _phoneNoList

    private val _availableFriends = MutableLiveData<MutableList<Account>>()
    val availableFriends: LiveData<MutableList<Account>> = _availableFriends

    fun loadContacts(phoneNo: String, contentResolver: ContentResolver, replaceCountryCode: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {

            val map = ContactUtils.getAllContacts(contentResolver, replaceCountryCode)
            _contactsList.postValue(map)
            // Only phone no to query unique users
            _phoneNoList.postValue(map.keys.toList())
            findFriends(phoneNo)
        }
    }

    private fun findFriends(phoneNo: String) {

        FirestoreUtils.getProfiles(phoneNo).get().addOnSuccessListener { snap ->

            viewModelScope.launch(Dispatchers.IO) {

                val matchedContacts = snap.documents.filter { doc ->
                    _phoneNoList.value!!.any { phoneNo ->
                        doc.id == phoneNo
                    }
                }

                val list = arrayListOf<Account>()

                matchedContacts.forEach { doc ->
                    val phoneContact = _contactsList.value!![doc.id]
                    val data = doc.data!!
                    val profile = Profile(
                        data["uid"] as String,
                        data["name"] as String,
                        data["phoneNo"] as String
                    )

                    list.add(Account(profile, phoneContact!!))
                }
                _availableFriends.postValue(list)
            }
        }
    }
}