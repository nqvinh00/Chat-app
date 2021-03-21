package com.group24.chatapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    companion object {
        const val LOGIN_TAG = "LoginActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login.setOnClickListener {
            val email = login_email.text.toString()
            val password = login_password.text.toString()

            Log.d(LOGIN_TAG, "Email: $email")
            Log.d(LOGIN_TAG, "Password: $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {

                }

        }

        back_to_register.setOnClickListener {
            finish()
        }
    }
}