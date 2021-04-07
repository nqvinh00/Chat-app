package com.group24.chatapp.models.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group24.chatapp.R
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_messages.view.*

class LatestMessageObject(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var chatUser : User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message.text = chatMessage.text

        val chatUserId : String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else {
            chatMessage.fromId
        }

        val reference = FirebaseDatabase.getInstance().getReference("/users/$chatUserId")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.latest_message_username.text = chatUser?.username

                val targetImageView = viewHolder.itemView.latest_message_img
                Picasso.get().load(chatUser?.profileImageURL).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_messages
    }
}