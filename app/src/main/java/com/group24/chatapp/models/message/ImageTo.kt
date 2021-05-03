package com.group24.chatapp.models.message

import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.image_to.view.*

class ImageTo(private val path: String, private val toUser: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val targetImageMessage = viewHolder.itemView.image_message_to
        Picasso.get().load(path).into(targetImageMessage)
        val uri = toUser.profileImageURL
        val targetImageView = viewHolder.itemView.ava_image_to
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.image_to
    }
}