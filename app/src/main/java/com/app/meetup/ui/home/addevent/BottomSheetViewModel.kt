package com.app.meetup.ui.home.addevent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.meetup.Invite
import com.app.meetup.repo.Repository

class BottomSheetViewModel : ViewModel() {

    private val repo = Repository.getInstance()
    val friends = repo.friends

    val inviteList = MutableLiveData<List<Invite>>()
    val selectedInvites = MutableLiveData<List<Invite>>()

//    fun invertInvite(index: Int) {
//        invites.value?.let { list ->
//            val value = list[index].isInvited
//            list[index].isInvited = !value
//            invites.postValue(list)
//        }
//    }

    fun resetInvites() {
        inviteList.value?.forEach { invite ->
            invite.isInvited = false
        }
    }
}