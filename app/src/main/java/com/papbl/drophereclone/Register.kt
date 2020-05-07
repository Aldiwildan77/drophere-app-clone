package com.papbl.drophereclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    lateinit var nameLayout: TextInputLayout
    lateinit var emailLayout: TextInputLayout
    lateinit var passwordLayout: TextInputLayout
    lateinit var confirmPasswordLayout: TextInputLayout
    lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        nameLayout = findViewById(R.id.til_register_name)
        emailLayout = findViewById(R.id.til_register_email)
        passwordLayout = findViewById(R.id.til_register_password)
        confirmPasswordLayout = findViewById(R.id.til_register_confirm_password)
        registerButton = findViewById(R.id.btn_register_account)

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v?.id == registerButton.id){
            mAuth!!.createUserWithEmailAndPassword(emailLayout.editText?.text.toString(),passwordLayout.editText?.text.toString())
        }
    }


}
