package com.papbl.drophereclone

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ForgotPassword : AppCompatActivity(), View.OnClickListener {

    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var emailButtonReset: MaterialButton
    private lateinit var emailEditText: TextInputLayout
    private var isEmailValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailButtonReset = findViewById(R.id.btn_reset_password)
        emailEditText = findViewById(R.id.til_reset_password)

        emailEditText.editText?.addTextChangedListener(emailWatcher)
        emailButtonReset.setOnClickListener(this)
    }

    private val emailWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            validateEmail(emailEditText.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validateEmail(emailEditText.editText?.text.toString())
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == emailButtonReset.id) {
            if (isEmailValid) forgotPassword(emailEditText.editText?.text.toString())
        }
    }

    private fun validateEmail(email: String = "") {
        when {
            email.isEmpty() -> {
                emailEditText.error =
                    resources.getString(R.string.error_reset_password_email_is_empty)
                isEmailValid = false
            }
            !isEmailValid(email) -> {
                emailEditText.error =
                    resources.getString(R.string.error_reset_password_email_not_valid)
                isEmailValid = false
            }
            else -> {
                emailEditText.error = null
                isEmailValid = true
            }
        }
    }

    private fun forgotPassword(email: String = "") {
        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { _ ->
                Toast.makeText(
                    this,
                    resources.getString(R.string.toast_reset_password_success),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(
                    this,
                    resources.getString(R.string.toast_reset_password_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
