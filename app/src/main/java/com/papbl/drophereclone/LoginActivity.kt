package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.papbl.drophereclone.models.Credential
import com.papbl.drophereclone.utils.UserCredential


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var resetPasswordButton: MaterialButton

    private val credential = UserCredential()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailLayout = findViewById(R.id.til_login_email)
        passwordLayout = findViewById(R.id.til_login_password)
        loginButton = findViewById(R.id.btn_login_submit)
        registerButton = findViewById(R.id.btn_register_intent)
        resetPasswordButton = findViewById(R.id.btn_reset_password)

        bindEvent()
    }

    override fun onStart() {
        super.onStart()
        println("credential ${credential.getLoggedUser(this)}")
        if (credential.getLoggedUser(this).uid.isNotEmpty()) {
            startActivity(Intent(this, OnBoardingActivity::class.java))
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
    }

    private fun bindEvent() {
        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
        resetPasswordButton.setOnClickListener(this)
    }

    @Suppress("LABEL_NAME_CLASH")
    override fun onClick(v: View?) {
        when (v?.id) {
            loginButton.id -> {

                if (emailLayout.editText?.text.isNullOrEmpty() || passwordLayout.editText?.text.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.toast_login_auth_is_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val email = emailLayout.editText?.text.toString()
                val password = passwordLayout.editText?.text.toString()
                var isError = false

                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        userCollection.get()
                            .addOnSuccessListener { result ->
                                result.forEach { user ->
                                    if (user["user_id"].toString() == authResult.user!!.uid) {
                                        credential.setLoggedUser(
                                            this,
                                            Credential(
                                                email,
                                                user["user_id"].toString(),
                                                user["fullname"].toString()
                                            )
                                        )
                                        startActivity(Intent(this, OnBoardingActivity::class.java))
                                        finish()
                                        return@addOnSuccessListener
                                    }
                                }
                                isError = true
                            }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.error_login_auth_is_not_valid),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                if (isError) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.error_login_auth_is_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            registerButton.id -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
            resetPasswordButton.id -> {
                startActivity(Intent(this, ForgotPassword::class.java))
            }
        }
    }

}
