package com.group24.chatapp.messages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.group24.chatapp.MenuActivity.Companion.currentUser
import com.group24.chatapp.R
import com.group24.chatapp.models.group.Group
import com.group24.chatapp.models.message.*
import com.group24.chatapp.models.user.User
import com.group24.chatapp.util.WhiteBoard
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val CHAT_LOG_TAG = "ChatLog"
        var user: User? = null
        var group: Group? = null
    }

    var adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        message_recycler_view.adapter = adapter
        message_recycler_view.scrollToPosition(adapter.itemCount - 1)
        user = intent.getParcelableExtra(NewMessage.USER_KEY)
        if (user != null) {
            supportActionBar?.title = user?.username
        } else {
            group = intent.getParcelableExtra(NewGroup.GROUP_KEY)
            supportActionBar?.title = group?.groupName
        }

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

        util_menu.setOnClickListener {
            if (voice_call.visibility == View.INVISIBLE) {
                menuDisplay(View.VISIBLE)
                menuAction()
            } else {
                menuDisplay(View.INVISIBLE)
            }
        }
    }

    private fun setupCall(path : String) {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        val channelName = UUID.randomUUID().toString()
        val reference = FirebaseDatabase.getInstance().getReference(path)
        reference.child("$fromId-$toId").setValue(channelName)
    }

    private fun menuDisplay(visibility : Int) {
        voice_call.visibility = visibility
        video_call.visibility = visibility
        whiteboard.visibility = visibility
    }

    private fun menuAction() {
        video_call.setOnClickListener {
            setupCall("/video-call")
            val intent = Intent(this, VideoCall::class.java)
            startActivity(intent)
            menuDisplay(View.INVISIBLE)
        }

        voice_call.setOnClickListener {
            setupCall("/voice-call")
            val intent = Intent(this, VoiceCall::class.java)
            if (user != null) intent.putExtra("target", "1v1")
            else intent.putExtra("target", "group")
            startActivity(intent)
            menuDisplay(View.INVISIBLE)

        }

        whiteboard.setOnClickListener {
            val intent = Intent(this, WhiteBoard::class.java)
            startActivityForResult(intent, 0)
            menuDisplay(View.INVISIBLE)
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val to: String? = if (user != null) {
            user!!.uid
        } else {
            group!!.groupName
        }
        val reference = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$to")
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.value.toString().contains("https://")) {
                    val image = snapshot.getValue(ImageMessage::class.java) ?: return
                    if (image.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = currentUser ?: return
                        adapter.add(ImageFrom(image.path, currentUser))
                    } else {
                        if (user != null) adapter.add(ImageTo(image.path, user!!))
                        else {
                            for (u in group!!.users!!) {
                                if (u.uid == image.fromId) {
                                    adapter.add(ImageTo(image.path, u))
                                    break
                                }
                            }
                        }
                    }
                }
                else {
                    val message = snapshot.getValue(ChatMessage::class.java) ?: return
                    Log.d(CHAT_LOG_TAG, message.text)
                    Log.d(CHAT_LOG_TAG, message.fromId + " " + FirebaseAuth.getInstance().uid)
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = currentUser ?: return
                        adapter.add(MessageFrom(message.text, currentUser))
                    } else {
                        if (user != null) adapter.add(MessageTo(message.text, user!!))
                        else {
                            for (u in group!!.users!!) {
                                if (u.uid == message.fromId) {
                                    adapter.add(MessageTo(message.text, u))
                                    break
                                }
                            }
                        }
                    }
                }
                message_recycler_view.scrollToPosition(adapter.itemCount - 1)
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
        Log.d(CHAT_LOG_TAG, message)
        val fromId = FirebaseAuth.getInstance().uid
        if (fromId == null || message.trim().isEmpty()) return
        val targetReference: String?
        val to: String?
        if (user != null) {
            to = user!!.uid
            targetReference = "latest-messages"
        } else {
            to = group!!.groupName
            targetReference = "latest-group-messages"
        }

        val reference = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$to").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/message-between/$to/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!, message, fromId, to, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(CHAT_LOG_TAG, "Save message to firebase ${reference.key}")
                    message_input.text.clear()
                    message_recycler_view.scrollToPosition(adapter.itemCount - 1)
        }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/$targetReference/$fromId/$to")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/$targetReference/$to/$fromId")
        latestMessageToRef.setValue(chatMessage)

        if (to.contains(currentUser!!.username)) {
            for (u in group!!.users!!) {
                if (u.username != currentUser!!.username) {
                    val uid = u.uid
                    val groupMessageRef = FirebaseDatabase.getInstance().getReference("/$targetReference/$uid/$to")
                    val toReference = FirebaseDatabase.getInstance().getReference("/message-between/$uid/$to").push()
                    toReference.setValue(chatMessage)
                    groupMessageRef.setValue(chatMessage)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(CHAT_LOG_TAG, "Photo was selected")
            val currentUser = currentUser ?: return
            val fromId = currentUser.uid
            val to : String? = if (user != null) {
                user!!.uid
            } else {
                group!!.groupName
            }

            val uuid = UUID.randomUUID().toString()
            val storagePath = "https://firebasestorage.googleapis.com/v0/b/kotlinchatapp-group24.appspot.com/o/images%2F$fromId%2F$to%2F$uuid?alt=media"
            val reference = FirebaseStorage.getInstance().getReference("/images/$fromId/$to/$uuid")
            reference.putFile(Uri.parse(data.data.toString()))
                .addOnSuccessListener {
                    reference.downloadUrl.addOnSuccessListener {
                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$fromId/$to").push()
                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, to!!, System.currentTimeMillis() / 1000)
                        db.setValue(imageMessage)
                            .addOnSuccessListener {

                            }
                    }
                }
                .addOnFailureListener {
                }
            val toReference = FirebaseStorage.getInstance().getReference("/images/$to/$fromId/$uuid")
            toReference.putFile(Uri.parse(data.data.toString()))
                    .addOnSuccessListener {
                        toReference.downloadUrl.addOnSuccessListener {
                            val db = FirebaseDatabase.getInstance().getReference("/message-between/$to/$fromId").push()
                            val imageMessage = ImageMessage(db.key!!, storagePath, fromId, to!!, System.currentTimeMillis() / 1000)
                            db.setValue(imageMessage)
                                    .addOnSuccessListener {

                                    }
                        }
                    }
            if (to!!.contains(currentUser.username)) {
                for (u in group!!.users!!) {
                    if (u.username != currentUser.username) {
                        val uid = u.uid
                        val toReference = FirebaseStorage.getInstance().getReference("/images/$uid/$to/$uuid")
                        toReference.putFile(Uri.parse(data.data.toString()))
                                .addOnSuccessListener {
                                    toReference.downloadUrl.addOnSuccessListener {
                                        val db = FirebaseDatabase.getInstance().getReference("/message-between/$uid/$to").push()
                                        val imageMessage = ImageMessage(db.key!!, storagePath, fromId, uid, System.currentTimeMillis() / 1000)
                                        db.setValue(imageMessage)
                                                .addOnSuccessListener {
                                                }
                                    }
                                }
                    }
                }
            }

        }
    }
}

