package com.app.meetup

data class Profile(
    var name: String = "",
    var phoneNo: String = ""
)

data class Account(
    val profile: Profile,
    var isFriend: Boolean = false,
    var isRequestSent: Boolean = false,
    var hasSentFriendRequest: Boolean = false
)

data class UserData(
    var friends: List<String> = mutableListOf(),
    var requestSent: List<String> = mutableListOf(),
    var friendRequests: List<String> = mutableListOf()
)

data class Invite(
    val profile: Profile,
    var isInvited: Boolean = false
)