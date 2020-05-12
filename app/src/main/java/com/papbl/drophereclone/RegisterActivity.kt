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
import com.papbl.drophereclone.models.Credential
import com.papbl.drophereclone.models.User
import com.papbl.drophereclone.utils.UserCredential

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    private val credential = UserCredential()

    private lateinit var fullnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var registerButton: MaterialButton
    private lateinit var loginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullnameLayout = findViewById(R.id.til_register_name)
        emailLayout = findViewById(R.id.til_register_email)
        passwordLayout = findViewById(R.id.til_register_password)
        confirmPasswordLayout = findViewById(R.id.til_register_confirm_password)
        registerButton = findViewById(R.id.btn_register_submit)
        loginButton = findViewById(R.id.btn_register_account_exist)

        registerButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View?) {
        if (v?.id == registerButton.id) {

            if (fullnameLayout.editText?.text.isNullOrEmpty() ||
                emailLayout.editText?.text.isNullOrEmpty() ||
                passwordLayout.editText?.text.isNullOrEmpty() ||
                confirmPasswordLayout.editText?.text.isNullOrEmpty()
            ) {
                toastMessage(resources.getString(R.string.toast_login_register_is_empty))
                return
            }

            registerNewUser { isSuccess ->
                if (isSuccess) {
                    val fullname = fullnameLayout.editText?.text.toString()
                    storeNewUserData(fullname, mAuth!!.uid!!)
                } else {
                    toastMessage(resources.getString(R.string.error_register_failed))
                }
            }
        } else if (v?.id == loginButton.id) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
        val user = User(fullname, userId)
        userCollection.add(user)
            .addOnSuccessListener {
                credential.setLoggedUser(
                    this,
                    Credential(
                        emailLayout.editText?.text.toString(),
                        userId,
                        fullname
                    )
                )
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                mAuth?.currentUser?.delete()
            }
    }

    private fun toastMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}
