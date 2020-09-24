package com.app.meetup

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.meetup.repo.Repository
import com.app.meetup.utils.ContactUtils
import com.app.meetup.utils.combineContacts
import com.app.meetup.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository.getInstance()

    val allContacts = MutableLiveData<HashMap<String, Profile>>()

    // Contacts that are on Meetup, these accounts has unset @param isFriend
    val availableFriends = MutableLiveData<MutableList<Account>>()


    // Meetup contacts that are added as friends by user

    val friends = repo.friends
    val friendRequests = MutableLiveData<MutableList<Account>>()
    val requestSent = MutableLiveData<MutableList<Account>>()

    // Friends become available when both UserData and Profiles are loaded
    val newContacts =
        friends.combineContacts(allContacts) { contactMaps, friendsList ->

            val profiles = repo.profiles.value!!

            repo.userData.value?.let { userData ->

                val requests = userData.friendRequests.map { req ->
                    Account(profiles.first { p -> req == p.phoneNo }).apply {
                        hasSentFriendRequest = true
                    }
                }.toMutableList()

                val sentRequests = userData.requestSent.map { req ->
                    Account(profiles.first { p -> req == p.phoneNo }).apply {
                        isRequestSent = true
                    }
                }.toMutableList()

                val myFriends = friends.value!!.map { p ->
                    // Since the use is already a friend then don't show it twice as a contact (unfriend)
                    contactMaps.remove(p.phoneNo)
                    Account(p).apply {
                        isFriend = true
                    }
                }.toMutableList()

                // Now since we removed all friends from contacts, see if any contact is on meet up
                val matchedContacts = profiles.mapNotNull { p ->
                    contactMaps.getOrElse(p.phoneNo, { null })
                }.toMutableList()

                // Add new contacts as well
                matchedContacts.forEach { p ->
                    myFriends.add(Account(p))
                }

                friendRequests.postValue(requests)
                requestSent.postValue(sentRequests)

                myFriends
            }
        }


    fun loadContacts(
        phoneNo: String?,
        contentResolver: ContentResolver,
        replaceCountryCode: Boolean
    ) {
        // When activity is first launched  with no phoneNo and no permissions
        if (phoneNo == null)
            return
        log("Loadin contacts now")
        viewModelScope.launch(Dispatchers.IO) {

            val map = ContactUtils.getAllContacts(contentResolver, replaceCountryCode)
            // If user have saved his own phoneNo, he can't add himself as friend
            map.remove(phoneNo)
            allContacts.postValue(map)
//            findFriends()
        }
    }


    private fun findFriends() {

        viewModelScope.launch(Dispatchers.IO) {
            val phoneList = allContacts.value!!.keys
            val profiles = repo.profiles.value!!
            val matchedContacts = profiles.filter { p ->
                phoneList.any { phoneNo ->
                    p.phoneNo == phoneNo
                }
            }

            val list = matchedContacts.map { p ->
                Account(p)
            }.toMutableList()

            availableFriends.postValue(list)
        }
    }

    fun updateContactStatus(old: Account, new: Account, index: Int) {
        viewModelScope.launch {
            newContacts.value?.let { contacts ->

                if (contacts.remove(old))
                    contacts.add(index, new)
            }
        }
    }
}