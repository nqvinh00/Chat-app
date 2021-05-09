package com.group24.chatapp.models.group

import android.widget.CheckBox
import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.group_users_list.view.*
import kotlinx.android.synthetic.main.users_list.view.image_profile_new_message
import kotlinx.android.synthetic.main.users_list.view.username_new_message

class UserObject(val user: User): Item<GroupieViewHolder>() {
    private var checkbox : CheckBox? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        checkbox = viewHolder.itemView.selected_user
        Picasso.get().load(user.profileImageURL).into(viewHolder.itemView.image_profile_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.group_users_list
    }

    fun setChecked(checked : Boolean) {
        checkbox!!.isChecked = checked
    }

    fun isChecked() : Boolean {
        return checkbox!!.isChecked
    }
}