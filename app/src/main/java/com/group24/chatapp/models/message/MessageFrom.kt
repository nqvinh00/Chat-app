package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.message_from.view.*
import kotlinx.android.synthetic.main.message_from.view.message_image_view

class MessageFrom(private val message: String, private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_from.text = message

        // Them anh dai dien vao chatlog
        val uri = user.profileImageURL
        val targetImageView = viewHolder.itemView.message_image_view
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.message_from
    }
}