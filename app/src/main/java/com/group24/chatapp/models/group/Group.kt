package com.group24.chatapp.models.group

import android.net.Uri
import android.os.Parcelable
import com.group24.chatapp.models.user.User
import kotlinx.android.parcel.Parcelize

@Parcelize
class Group(val groupName: String, val users : ArrayList<User>?): Parcelable {
    constructor() : this("", null)
}