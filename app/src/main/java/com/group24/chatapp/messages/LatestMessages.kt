package com.group24.chatapp.messages

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.group24.chatapp.R
import com.group24.chatapp.RegisterActivity
import com.group24.chatapp.messages.NewMessage.Companion.USER_KEY
import com.group24.chatapp.models.message.ChatMessage
import com.group24.chatapp.models.message.LatestMessageObject
import com.group24.chatapp.models.user.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessages : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        private val adapter = GroupAdapter<GroupieViewHolder>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        latest_messages_recycler_view.adapter = adapter
        latest_messages_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // set item click listener on adapter
        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val userItem = item as LatestMessageObject
            intent.putExtra(USER_KEY, userItem.chatUser)
            startActivity(intent)
        }
        
        menu_button.setOnClickListener {
            menuDisplay(View.VISIBLE)
            menuAction()
        }

        verifyLogin()
        fetchCurrentUser()
        listenForLatestMessages()
    }

    private fun menuAction() {
        new_message_button.setOnClickListener {
            val intent = Intent(this, NewMessage::class.java)
            startActivity(intent)
            menuDisplay(View.INVISIBLE)
        }
    }

    private fun menuDisplay(visibility : Int) {
        new_message_button.visibility = visibility
        group_chat_button.visibility = visibility
    }

    val latestMessageList = HashMap<String, ChatMessage>()
    private fun refreshRecycleViewMessages() {
        adapter.clear()
        latestMessageList.values.forEach {
            adapter.add(LatestMessageObject(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

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

    // fetch the current user
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot : DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(p0 : DatabaseError) {
            }
        })
    }

    private fun verifyLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}