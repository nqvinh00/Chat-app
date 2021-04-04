package com.group24.chatapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.group24.chatapp.R
import com.group24.chatapp.models.message.*
import com.group24.chatapp.models.user.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val CHAT_LOG_TAG = "ChatLog"
    }

    private var adapter = GroupAdapter<GroupieViewHolder>()
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        message_recycler_view.adapter = adapter
        user = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        supportActionBar?.title = user?.username

        listenForMessages()

        send_message_button.setOnClickListener {
            Log.d(CHAT_LOG_TAG, "Send message")
            sendMessage()
        }

        choose_image.setOnClickListener {
            Log.d(CHAT_LOG_TAG, "Choose image")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$toId")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                Log.i(CHAT_LOG_TAG, "addChildEventListener called")
                if (message != null) {
                    Log.d(CHAT_LOG_TAG, message.text)
                    Log.d(CHAT_LOG_TAG, message.fromId + " " + FirebaseAuth.getInstance().uid)
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessages.currentUser ?: return
                        adapter.add(MessageFrom(message.text, currentUser))
                    } else {
                        adapter.add(MessageTo(message.text, user!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun sendMessage() {
        val message = message_input.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        val toId = user!!.uid

        if (fromId == null) return
        val reference = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/message-between/$toId/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!, message, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(CHAT_LOG_TAG, "Save message to firebase ${reference.key}")
                    message_input.text.clear()
                    message_recycler_view.scrollToPosition(adapter.itemCount - 1)
        }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}

