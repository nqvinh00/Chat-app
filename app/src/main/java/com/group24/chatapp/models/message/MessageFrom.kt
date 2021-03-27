package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.message_from.view.*

class MessageFrom(private val message: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_from.text = message
    }

    override fun getLayout(): Int {
        return R.layout.message_from
    }
}