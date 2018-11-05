package com.zisarknar.kotlinmessenger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            var email = email_edittext_login.text.toString()
            var password = password_edittext_login.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener {
                Log.d("Login", "Login in success: ${it.user.email}")
            }.addOnFailureListener {
                Log.d("Login", "Login failure: ${it.message}")
            }
        }


        back_to_register_textview.setOnClickListener {
            finish()
        }
    }
}