package com.example.pawfectmatchapp.models

data class FeedItem(
    val userId: String,
    val userName: String,
    val userProfileImageUrl: String,
    val sharedDogs: List<String>,
    var isExpanded: Boolean = false
)
