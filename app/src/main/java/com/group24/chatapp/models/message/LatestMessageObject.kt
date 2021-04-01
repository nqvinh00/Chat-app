package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessageObject: Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.latest_messages
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

}