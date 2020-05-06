package com.papbl.drophereclone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.papbl.drophereclone.R


class UserProfile : Fragment(), View.OnClickListener {

    private val db = FirebaseFirestore.getInstance()
    lateinit var currenttUser: FirebaseUser

    private val userId: String = "3sRZKMwAnEYJayzHt3oqQXsnrmv2"

    private lateinit var tfUserName: TextInputLayout
    private var userNameCondition: Boolean = false
    private lateinit var tfEmailAddres: TextInputLayout
    private var emailAddressCondition: Boolean = false
    private lateinit var btnSaveProfile: Button
    private lateinit var tfCurrentPassword: TextInputLayout
    private lateinit var tfNewPassword: TextInputLayout
    private lateinit var tfConfirmPassword: TextInputLayout
    private lateinit var btnSavePassword: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.user_profile, container, false)

    private val userNameWatch: TextWatcher = object : TextWatcher {
        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            userNameCondition = true
            btnSaveProfile.isEnabled = true
        }
    }

    private val emailAddressWatch: TextWatcher = object : TextWatcher {
        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            emailAddressCondition = true
            btnSaveProfile.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        currenttUser = getCurrentUser()!!

        //getUserName { isSuccess, fullname -> tfUserName.editText?.setText(fullname) }
        tfUserName.editText?.setText("Gilang Ganteng")
        tfEmailAddres.editText?.setText(currenttUser.email)
        tfUserName.editText?.addTextChangedListener(userNameWatch)
        tfEmailAddres.editText?.addTextChangedListener(emailAddressWatch)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        login()

        tfUserName = activity?.findViewById(R.id.tf_userName)!!
        tfEmailAddres = activity?.findViewById(R.id.tf_alamatEmail)!!
        btnSaveProfile = activity?.findViewById(R.id.btn_simpanProfile)!!
        btnSaveProfile.setOnClickListener(this)

        tfCurrentPassword = activity?.findViewById(R.id.tf_currentPass)!!
        tfNewPassword = activity?.findViewById(R.id.tf_newPass)!!
        tfConfirmPassword = activity?.findViewById(R.id.tf_konfirmasi)!!
        btnSavePassword = activity?.findViewById(R.id.btn_simpanPassword)!!
    }

    fun login() {
        val email = "nuraidigilang@gmail.com"
        val password = "gilangganteng"
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
    }

    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun getUserName(callback: (isSuccess: Boolean, fullname: String) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { data ->
                data.forEach { result ->
                    if (result["user_id"].toString() == userId) {
                        callback.invoke(true, result["fullname"].toString())
                    }
                }
            }.addOnFailureListener {
                callback.invoke(false, "")
            }
    }

    fun getData(callback: (isSuccess: Boolean, userData: String) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { data ->
                data.forEach { result ->
                    if (result["user_id"].toString() == userId) {
                        println("USER-ID : " + result.id)
                        callback.invoke(true, result.id)
                    }
                }
            }.addOnFailureListener {
                callback.invoke(false, "")
            }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_simpanProfile) {
            println("CONDITION" + userNameCondition)
            if (userNameCondition) {
                getData { isSuccess, id ->
                    db.collection("users").document(id)
                        .update("fullname", tfUserName.editText?.text.toString())
                }
            }
        }
    }

}