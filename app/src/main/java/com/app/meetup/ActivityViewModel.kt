package com.app.meetup

import android.app.Application
import android.content.ContentResolver
import android.util.TimeUtils
import androidx.lifecycle.*
import com.app.meetup.repo.Repository
import com.app.meetup.utils.ContactUtils
import com.app.meetup.utils.combineContacts
import com.app.meetup.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository.getInstance()

    val inviteTemplate = Transformations.map(repo.inviteTemplate) { inviteMsg ->
        try {
            inviteMsg.message?.replace("{link}", inviteMsg.link!!)
        } catch (e: Exception) {
            null
        }
    }

    val allContacts = MutableLiveData<HashMap<String, Profile>>().apply {
        postValue(hashMapOf())
    }
    val empty = repo.empty

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

                val requests = userData.friendRequests.mapNotNull { req ->
                    profiles.firstOrNull { p -> req == p.phoneNo }?.let { p ->
                        Account(p).apply {
                            hasSentFriendRequest = true
                        }
                    }
                }.toMutableList()

                val sentRequests = userData.requestSent.mapNotNull { req ->
                    profiles.firstOrNull { p -> req == p.phoneNo }?.let { p ->
                        Account(p).apply {
                            isRequestSent = true
                        }
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
                matchedContacts.forEachIndexed { i, p ->
                    val isInRequests = requests.any { a -> a.profile.phoneNo == p.phoneNo }
                    val isInSentRequests = sentRequests.any { a -> a.profile.phoneNo == p.phoneNo }
                    // Don't show in friends if user has already sent request or use has sent one
                    if (isInRequests || isInSentRequests)
                        matchedContacts.removeAt(i)
                    else
                        myFriends.add(Account(p))
                }

                friendRequests.postValue(requests)
                requestSent.postValue(sentRequests)

                myFriends
            }
        }

    val invitesList = newContacts.asFlow()
        .flowOn(Dispatchers.IO).transformLatest { list ->

            val accounts = allContacts.value?.mapNotNull { item ->
                Account(item.value)
            }
            emit(list?.union(accounts!!)?.toList())

        }.asLiveData(Dispatchers.IO)

    var start: Long = 0L

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

//            val map = ContactUtils.getAllContacts(contentResolver, replaceCountryCode)
            // If user have saved his own phoneNo, he can't add himself as friend
//            map.remove(phoneNo)
            ContactUtils.fetchContacts(contentResolver, replaceCountryCode)
//                .onStart {
//                    start = System.currentTimeMillis() / 1000
//                }
//                .onCompletion {
//                    log("Time ended = ${System.currentTimeMillis() / 1000} ms")
//                    log("Duration = ${(System.currentTimeMillis() / 1000) - start} ms")
//                }
                .collect { profile ->
                    if (profile.phoneNo != phoneNo) {
                        allContacts.value?.put(profile.phoneNo, profile)
                    }
                }
//            allContacts.postValue(map)
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