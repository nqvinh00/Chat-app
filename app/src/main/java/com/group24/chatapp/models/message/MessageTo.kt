package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.message_to.view.*

class MessageTo(private val message: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_to.text = message
    }

    override fun getLayout(): Int {
        return R.layout.message_to
    }
}