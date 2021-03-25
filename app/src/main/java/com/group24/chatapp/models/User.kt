package com.group24.chatapp.models

class User(val uid: String, val username: String, val profileImageURL: String) {
    constructor() : this("", "", "")
}