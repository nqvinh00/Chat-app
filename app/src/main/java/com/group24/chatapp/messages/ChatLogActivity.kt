package com.group24.chatapp.messages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.group24.chatapp.R
import com.group24.chatapp.models.message.*
import com.group24.chatapp.models.user.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val CHAT_LOG_TAG = "ChatLog"
    }

    var adapter = GroupAdapter<GroupieViewHolder>()
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

        listenForImages()
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

    private fun listenForImages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/images/$fromId/$toId")
        Log.d("test", "test")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val image = snapshot.getValue(ImageMessage::class.java)
                Log.i(CHAT_LOG_TAG, "addChildEventListener called")
                if (image != null) {
                    Log.d(CHAT_LOG_TAG, image.path.toString())
                    Log.d(CHAT_LOG_TAG, image.fromId + " " + FirebaseAuth.getInstance().uid)
                    if (image.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessages.currentUser ?: return
                        adapter.add(ImageFrom(image.path, currentUser, user!!))
                    } else {
                        adapter.add(ImageTo(image.path, user!!, fromId!!))
                    }
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(CHAT_LOG_TAG, "Photo was selected")
            val currentUser = LatestMessages.currentUser ?: return
            val fromId = currentUser.uid
            val toId = user!!.uid
            val uuid = UUID.randomUUID().toString()
            val storagePath = "https://firebasestorage.googleapis.com/v0/b/kotlinchatapp-group24.appspot.com/o/images%2F$fromId%2F$toId%2F$uuid?alt=media"
            val reference = FirebaseStorage.getInstance().getReference("/images/$fromId/$toId/$uuid")
            val toReference = FirebaseStorage.getInstance().getReference("/images/$toId/$fromId/$uuid")
            reference.putFile(Uri.parse(data.data.toString()))
                .addOnSuccessListener { it ->
                    Log.d("Image message", "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    reference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/images/$fromId/$toId").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, toId, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                            .addOnSuccessListener {

                            }
                    }
                }
                .addOnFailureListener {
                }
            toReference.putFile(Uri.parse(data.data.toString()))
                .addOnSuccessListener {
                    Log.d("Image message", "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    toReference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/images/$toId/$fromId").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, toId, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                            .addOnSuccessListener {

                        }
                    }
                }
        }
    }

}

