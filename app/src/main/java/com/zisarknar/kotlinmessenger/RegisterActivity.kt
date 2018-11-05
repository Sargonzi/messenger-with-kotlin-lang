package com.zisarknar.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            registerNewUser()
        }

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btn_select.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            btn_image.setImageBitmap(bitmap)
            btn_select.alpha = 0f
        }
    }

    private fun registerNewUser() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            email_edittext_register.error = "Email is required"
            password_edittext_register.error = "Password is required"
            return
        }

        Log.d("Main", "Email: $email")
        Log.d("Main", "Password: $password")

        //Authentication with firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener

            //else if it's successful
            Log.d("Register", "User is successfully created with uid: ${it.result.user.uid}")

            uploadImagetoFirebaseStorage()
        }
            .addOnFailureListener {
                Log.d("Register", "Create new user failur ${it.message}")
                Toast.makeText(this, "Fail to regiser: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImagetoFirebaseStorage() {

        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val rel = FirebaseStorage.getInstance().getReference("/images/$filename")
        rel.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("Register", "Succefully uploaded ${it.metadata?.path}")

            rel.downloadUrl.addOnSuccessListener {

                Log.d("Register", "Uploaded  Failed ${it.toString()}")
                saveUserToFirebaseDatabase(it.path)
            }
        }.addOnFailureListener{
            Log.d("Register", "Uploaded  Failed ${it.message}")
        }

    }

    private fun saveUserToFirebaseDatabase(photoUrlString: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), photoUrlString)

        ref.setValue(user)
            .addOnSuccessListener {
            Log.d("Register", "Finally we have saved you user to database")
        }
            .addOnFailureListener {
                Log.d("Register", "Failed to upload to database ${it.message}")
            }
    }
}

class User(val uid: String, val username: String, val profileImageUrl:String)