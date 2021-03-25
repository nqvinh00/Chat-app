package com.group24.chatapp.models

import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder

class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout(): Int {
        TODO("Not yet implemented")
    }
}