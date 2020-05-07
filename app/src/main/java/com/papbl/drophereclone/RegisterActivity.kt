package com.papbl.drophereclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var registerButton: MaterialButton

    data class Users(
        val fullname: String,
        val user_id: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        nameLayout = findViewById(R.id.til_register_name)
        emailLayout = findViewById(R.id.til_register_email)
        passwordLayout = findViewById(R.id.til_register_password)
        confirmPasswordLayout = findViewById(R.id.til_register_confirm_password)
        registerButton = findViewById(R.id.btn_register_submit)

        registerButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (mAuth != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View?) {
        if (v?.id == registerButton.id) {
            registerNewUser { isSuccess ->
                if (isSuccess) {
                    val fullname = nameLayout.editText?.text.toString()
                    storeNewUserData(fullname, mAuth!!.uid!!)
                } else {
                    Toast.makeText(this, "Gagal untuk mendaftarkan akun", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun registerNewUser(callback: (isSuccess: Boolean) -> Unit) {
        val email = emailLayout.editText?.text.toString()
        val password = passwordLayout.editText?.text.toString()

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                mAuth!!.signInWithEmailAndPassword(email, password)
                callback.invoke(true)
            }.addOnFailureListener {
                callback.invoke(false)
            }
    }

    private fun storeNewUserData(fullname: String, userId: String) {
        val user = Users(fullname, userId)
        userCollection.add(user)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                mAuth?.currentUser?.delete()
            }
    }

}
