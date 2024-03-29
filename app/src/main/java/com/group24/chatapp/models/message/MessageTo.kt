package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.message_to.view.*

class MessageTo(private val message: String, private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_to.text = message

        val uri = user.profileImageURL
        val targetImageView = viewHolder.itemView.ava_message_to
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.message_to
    }
}