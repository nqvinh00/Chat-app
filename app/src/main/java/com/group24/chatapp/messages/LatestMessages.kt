package com.group24.chatapp.messages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.group24.chatapp.MenuActivity
import com.group24.chatapp.R
import com.group24.chatapp.messages.NewGroup.Companion.GROUP_KEY
import com.group24.chatapp.messages.NewMessage.Companion.USER_KEY
import com.group24.chatapp.models.message.ChatMessage
import com.group24.chatapp.models.message.LatestGroupMessageObject
import com.group24.chatapp.models.message.LatestMessageObject
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessages : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    var target : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        latest_messages_recycler_view.adapter = adapter
        latest_messages_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        target = intent.getStringExtra("TARGET")

        // set item click listener on adapter
        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, ChatLogActivity::class.java)
            if (target == "1v1") {
                val userItem = item as LatestMessageObject
                intent.putExtra(USER_KEY, userItem.chatUser)
            } else {
                val groupItem = item as LatestGroupMessageObject
                intent.putExtra(GROUP_KEY, groupItem.group)
            }
            startActivity(intent)
        }

        new_message_button.setOnClickListener {
            val intent = if (target == "1v1") {
                Intent(this, NewMessage::class.java)
            } else {
                Intent(this, NewGroup::class.java)
            }
            startActivity(intent)
        }

        listenForLatestMessages()
    }

    val latestMessageList = HashMap<String, ChatMessage>()
    private fun refreshRecycleViewMessages() {
        adapter.clear()
        if (target == "1v1") {
            latestMessageList.values.forEach {
                adapter.add(LatestMessageObject(it))
            }
        } else {
            latestMessageList.values.forEach {
                adapter.add(LatestGroupMessageObject(it, it.toId))
            }
        }

    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref: DatabaseReference?
        ref = if (target == "1v1") {
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        } else {
            FirebaseDatabase.getInstance().getReference("/latest-group-messages/$fromId")
        }

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageList[snapshot.key!!] = latestMessage
                refreshRecycleViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val latestMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageList[snapshot.key!!] = latestMessage
                refreshRecycleViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}