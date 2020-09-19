package com.app.meetup

data class Profile(
    val uid: String,
    val name: String,
    val phoneNo: String
)

data class Contact(
    val name: String,
    val phoneNo: String
)

data class Account(
    val profile: Profile,
    val contact: Contact,
    var isFriend: Boolean = false,
    var isRequestSent: Boolean = false,
    var hasSentFriendRequest: Boolean = false
)

data class UserData(
    var friends: List<String> = mutableListOf(),
    var requestSent: List<String> = mutableListOf(),
    var friendRequests: List<String> = mutableListOf()
)