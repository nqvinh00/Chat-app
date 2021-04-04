package com.group24.chatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.group24.chatapp.messages.LatestMessages
import com.group24.chatapp.models.user.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var selectedPhotoURI: Uri?= null
    companion object {
        const val REGISTER_TAG = "RegisterActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        register.setOnClickListener {
            register()
        }

        already_have_acc.setOnClickListener {
            moveToLoginScreen()
        }

        select_photo.setOnClickListener {
            Log.d(REGISTER_TAG, "Select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private fun moveToLoginScreen() {
        Log.d(REGISTER_TAG, "Move to login screen")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun register() {
        val email = email_register.text.toString()
        val username = username_register.text.toString()
        val password = password_register.text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Email/username/password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(REGISTER_TAG, "Email: $email")
        Log.d(REGISTER_TAG, "Username: $username")
        Log.d(REGISTER_TAG, "Password: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(REGISTER_TAG, "Register successfully, user uid ${it.result?.user?.uid}")
                uploadImageToFirebase()
                Toast.makeText(this, "Create account successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d(REGISTER_TAG, "Failed to create user:  ${it.message}")
                Toast.makeText(this, "Failed to create user:  ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoURI == null ) return
        val filename = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("/images/$filename")
        reference.putFile(selectedPhotoURI!!)
                .addOnSuccessListener { it ->
                    Log.d(REGISTER_TAG, "Upload avatar to firebase successfully: ${it.metadata?.path}")
                    reference.downloadUrl.addOnSuccessListener {
                        Log.d(REGISTER_TAG, "File location: $it")
                        saveUserToDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(REGISTER_TAG, "Fail to upload image to storage ${it.message}")
                }
    }

    private fun saveUserToDatabase(profileImageURL: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_register.text.toString(), profileImageURL)
        reference.setValue(user)
                .addOnSuccessListener {
                    Log.d(REGISTER_TAG, "Save user to database successfully")

                    val intent = Intent(this, LatestMessages::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d(REGISTER_TAG, "Fail to save user to database ${it.message}")
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(REGISTER_TAG, "Photo was selected")

            selectedPhotoURI = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoURI)
            select_photo_image_view.setImageBitmap(bitmap)
            select_photo.alpha = 0f
        }
    }
}
