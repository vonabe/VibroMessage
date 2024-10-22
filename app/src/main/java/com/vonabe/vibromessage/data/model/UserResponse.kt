package com.vonabe.vibromessage.data.model

data class UserResponse(
    val uniqueId: String,
    val friends: Set<String>? = null
)