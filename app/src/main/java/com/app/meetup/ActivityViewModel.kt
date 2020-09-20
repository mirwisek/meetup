package com.app.meetup

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.*
import com.app.meetup.repo.Repository
import com.app.meetup.utils.ContactUtils
import com.app.meetup.utils.FirestoreUtils
import com.app.meetup.utils.combineContacts
import com.app.meetup.utils.combineProfilesWithFriends
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

    // Contacts that are on Meetup & are in user's friend list
    val meetupContacts =
        repo.userData.combineContacts(availableFriends) { contactListFriends, userData ->

            val requests = mutableListOf<Account>()
            val sentRequests = mutableListOf<Account>()

            val filteredList = mutableListOf<Account>()

            contactListFriends.forEach { acc ->
                // If it is found in friends list then mark him friend (btnFriend || btnUnfriend)
                acc.isFriend = userData.friends.any { friend -> friend == acc.profile.phoneNo }
                acc.isRequestSent =
                    userData.requestSent.any { friend -> friend == acc.profile.phoneNo }
                acc.hasSentFriendRequest =
                    userData.friendRequests.any { friend -> friend == acc.profile.phoneNo }

                if (acc.isFriend) {
                    filteredList.add(acc)
                } else if (acc.isRequestSent)
                    sentRequests.add(acc)
                else if (acc.hasSentFriendRequest)
                    requests.add(acc)
                else // Add non friends, should be able to send them friend request
                    filteredList.add(acc)
            }

            friendRequests.postValue(requests)
            requestSent.postValue(sentRequests)

            filteredList
        }


    fun loadContacts(
        phoneNo: String,
        contentResolver: ContentResolver,
        replaceCountryCode: Boolean
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            val map = ContactUtils.getAllContacts(contentResolver, replaceCountryCode)
            // If user have saved his own phoneNo, he can't add himself as friend
            map.remove(phoneNo)
            allContacts.postValue(map)
            findFriends(phoneNo)
        }
    }


    private fun findFriends(phoneNo: String) {

        FirestoreUtils.getProfiles(phoneNo).get().addOnSuccessListener { snap ->

            viewModelScope.launch(Dispatchers.IO) {
                val phoneList = allContacts.value!!.keys
                val matchedContacts = snap.documents.filter { doc ->
                    phoneList.any { phoneNo ->
                        doc.id == phoneNo
                    }
                }

                val list = arrayListOf<Account>()

                matchedContacts.forEach { doc ->
//                    val phoneContact = allContacts.value!![doc.id]
                    doc.toObject(Profile::class.java)?.let { profile ->
                        list.add(Account(profile))
                    }
                }
                availableFriends.postValue(list)
            }
        }
    }

    fun updateContactStatus(old: Account, new: Account, index: Int) {
        viewModelScope.launch {
            meetupContacts.value?.let { contacts ->

                if (contacts.remove(old))
                    contacts.add(index, new)
            }
        }
    }
}