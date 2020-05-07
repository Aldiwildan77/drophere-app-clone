package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity(),View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    lateinit var emailLayout: TextInputLayout
    lateinit var passwordLayout: TextInputLayout
    lateinit var loginButton: MaterialButton
    lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailLayout = findViewById(R.id.til_login_email)
        passwordLayout = findViewById(R.id.til_login_password)
        loginButton = findViewById(R.id.btn_login)
        registerButton = findViewById(R.id.btn_register)

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == loginButton.id){
            mAuth!!.signInWithEmailAndPassword(
                emailLayout.editText?.text.toString(),passwordLayout.editText?.text.toString()
            ).addOnSuccessListener {
                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Email atau Password Salah!!" , Toast.LENGTH_SHORT)
                    .show()
            }
        }
        else if (v?.id == registerButton.id){
            startActivity(
                Intent(this, Register::class.java)
            )
            finish()
        }
    }



}
