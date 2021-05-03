package com.group24.chatapp.models.message

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group24.chatapp.R
import com.group24.chatapp.models.group.Group
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_messages.view.*

class LatestGroupMessageObject(private val chatMessage: ChatMessage, private val name : String) : Item<GroupieViewHolder>() {
    var group : Group? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message.text = chatMessage.text

        val groupName = name

        val reference = FirebaseDatabase.getInstance().getReference("/groups/$groupName")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                group = snapshot.getValue(Group::class.java)
                viewHolder.itemView.latest_message_username.text = group?.groupName

                val targetImageView = viewHolder.itemView.latest_message_img
                targetImageView.setBackgroundResource(R.drawable.baseline_group_24)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_messages
    }
}