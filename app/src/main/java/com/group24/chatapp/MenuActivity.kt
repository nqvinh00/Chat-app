package com.group24.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group24.chatapp.messages.LatestMessages
import com.group24.chatapp.models.user.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        message_1v1.setOnClickListener {
            val intent = Intent(this, LatestMessages::class.java)
            intent.putExtra("TARGET", "1v1")
            startActivity(intent)
        }

        message_group.setOnClickListener {
            val intent = Intent(this, LatestMessages::class.java)
            intent.putExtra("TARGET", "group")
            startActivity(intent)
        }

        verifyLogin()
        fetchCurrentUser()
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

    // fetch the current user
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot : DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("test", currentUser.toString())
                welcome_user.text = currentUser!!.username
                Picasso.get().load(currentUser!!.profileImageURL).into(user_image)
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
}