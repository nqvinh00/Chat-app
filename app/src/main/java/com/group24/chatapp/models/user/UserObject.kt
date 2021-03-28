package com.group24.chatapp.models.user

import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.users_list.view.*

class UserObject(val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageURL).into(viewHolder.itemView.image_profile_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.users_list
    }
}