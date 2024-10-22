package com.vonabe.vibromessage.data.model

data class MessageRequest(val token: String, val title: String, val body: String, val data: Map<String, String>?)
