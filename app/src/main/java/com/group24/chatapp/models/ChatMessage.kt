package com.group24.chatapp.models

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long
) {
    constructor() : this("", "", "", "", -1)
}

//class ChatMessage(val text: String)