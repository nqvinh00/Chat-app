package com.group24.chatapp.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group24.chatapp.MenuActivity
import com.group24.chatapp.R
import com.group24.chatapp.models.group.Group
import com.group24.chatapp.models.group.UserObject
import com.group24.chatapp.models.user.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_group_message.*
import kotlin.collections.ArrayList


class NewGroup : AppCompatActivity() {
    companion object {
        const val NEW_GROUP_TAG = "NewGroup"
        const val ACTION_BAR_TITLE = "Select Users"
        const val GROUP_KEY = "GROUP_KEY"
    }

    private var users = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group_message)
        supportActionBar?.title = ACTION_BAR_TITLE
        users.add(MenuActivity.currentUser!!)
        usersList()
    }

    private fun usersList() {
        val reference = FirebaseDatabase.getInstance().getReference("/users")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach { it ->
                    Log.d(NEW_GROUP_TAG, it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        adapter.add(UserObject(user))
                    }
                }

                adapter.setOnItemClickListener { item, _ ->
                    val userItem = item as UserObject
                    if (!userItem.isChecked() && !users.contains(userItem.user)) {
                        userItem.setChecked(true)
                        users.add(userItem.user)
                    } else {
                        userItem.setChecked(false)
                        users.remove(userItem.user)
                    }

                }

                new_group_message_recycler_view.adapter = adapter

                group_done.setOnClickListener {
                    var groupName= ""
                    val intent = Intent(this@NewGroup, ChatLogActivity::class.java)

                    users.sortByDescending { list -> list.username }
                    for (user in users) {
                            groupName += user.username + ","
                    }
                    groupName = groupName.slice(IntRange(0, groupName.length - 2))
                    val group = Group(groupName, users)
                    val reference = FirebaseDatabase.getInstance().getReference("/groups")
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(groupName)) {
                                Toast.makeText(this@NewGroup, "Group already existed", Toast.LENGTH_SHORT).show()
                                return
                            } else {
                                val childReference = FirebaseDatabase.getInstance().getReference("/groups/$groupName")
                                childReference.setValue(group)
                                    .addOnSuccessListener {
                                        intent.putExtra(GROUP_KEY, group)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@NewGroup, "Create group failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                            }
                        }
                    })
                }
            }

        })
    }
}