package com.group24.chatapp.models.message

class ImageMessage(
    val id: String,
    val path: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long
) {
    constructor() : this("", "", "", "", -1)
}