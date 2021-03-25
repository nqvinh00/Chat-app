package com.group24.chatapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group24.chatapp.R
import com.group24.chatapp.models.User
import com.group24.chatapp.models.UserObject
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessage : AppCompatActivity() {
    companion object {
        const val NEW_MESSAGE_TAG = "NewMessage"
        const val USERS_STORAGE_PATH = "/users"
        const val ACTION_BAR_TITLE = "Select User"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = ACTION_BAR_TITLE

        usersList()
    }

    private fun usersList() {
        val reference = FirebaseDatabase.getInstance().getReference(USERS_STORAGE_PATH)
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach { it ->
                    Log.d(NEW_MESSAGE_TAG, it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserObject(user))
                    }
                }

                new_message_recycler_view.adapter = adapter
            }
        })
    }
}