package com.app.meetup.ui.contacts

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.meetup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val _contactsList = MutableLiveData<HashMap<String, Contact>>()
    val contactsList: LiveData<HashMap<String, Contact>> = _contactsList

}