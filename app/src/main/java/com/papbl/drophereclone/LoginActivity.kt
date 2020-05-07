package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private lateinit var emailLayout: TextInputLayout
    lateinit var passwordLayout: TextInputLayout
    lateinit var loginButton: MaterialButton
    lateinit var registerButton: MaterialButton
    lateinit var resetPasswordButton: MaterialButton

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
        if (mAuth != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
    }

    private fun bindEvent() {
        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
        resetPasswordButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            loginButton.id -> {
                mAuth!!.signInWithEmailAndPassword(
                    emailLayout.editText?.text.toString(), passwordLayout.editText?.text.toString()
                ).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Email atau Password Salah!!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            registerButton.id -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
            resetPasswordButton.id -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }


}
