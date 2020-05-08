package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.papbl.drophereclone.utils.UserCredential


class ProfileFragment : Fragment(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    private val credential = UserCredential()

    private var userNameCondition: Boolean = false
    private var emailAddressCondition: Boolean = false

    private lateinit var tfUserName: TextInputLayout
    private lateinit var tfEmailAddres: TextInputLayout
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var tfCurrentPassword: TextInputLayout
    private lateinit var tfNewPassword: TextInputLayout
    private lateinit var tfConfirmPassword: TextInputLayout
    private lateinit var btnSavePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tfUserName = view.findViewById(R.id.til_profile_name)
        tfEmailAddres = view.findViewById(R.id.til_profile_email)
        btnSaveProfile = view.findViewById(R.id.btn_profile_submit)

        tfCurrentPassword = view.findViewById(R.id.til_profile_current_password)
        tfNewPassword = view.findViewById(R.id.til_profile_new_password)
        tfConfirmPassword = view.findViewById(R.id.til_profile_new_confirm_password)
        btnSavePassword = view.findViewById(R.id.btn_profile_submit_password)

        btnLogout = view.findViewById(R.id.btn_profile_logout)

        btnSaveProfile.setOnClickListener(this)
        btnSavePassword.setOnClickListener(this)
        btnLogout.setOnClickListener(this)

        return view
    }

    private val userNameWatch: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            userNameCondition = true
            btnSaveProfile.isEnabled = true
        }
    }

    private val emailAddressWatch: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            emailAddressCondition = true
            btnSaveProfile.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()

        getUserFullName { _, fullname ->
            tfUserName.editText?.setText(fullname)
            tfEmailAddres.editText?.setText(credential.getLoggedUser(requireActivity()).email)
        }

        tfUserName.editText?.addTextChangedListener(userNameWatch)
        tfEmailAddres.editText?.addTextChangedListener(emailAddressWatch)

    }

    private fun getUserFullName(callback: (isSuccess: Boolean, fullname: String) -> Unit) {
        userCollection
            .get()
            .addOnSuccessListener { data ->
                data.forEach { result ->
                    if (result["user_id"].toString() == credential.getLoggedUser(requireActivity()).uid) {
                        callback.invoke(true, result["fullname"].toString())
                    }
                }
            }.addOnFailureListener {
                callback.invoke(false, "")
            }
    }

    private fun getData(callback: (isSuccess: Boolean, userData: String) -> Unit) {
        userCollection
            .get()
            .addOnSuccessListener { data ->
                data.forEach { result ->
                    if (result["user_id"].toString() == credential.getLoggedUser(requireActivity()).uid) {
                        callback.invoke(true, result.id)
                    }
                }
            }.addOnFailureListener {
                callback.invoke(false, "")
            }
    }

    override fun onClick(v: View?) {
        if (v?.id == btnSaveProfile.id) {
            if (userNameCondition) {
                getData { _, id ->
                    userCollection
                        .document(id)
                        .update("fullname", tfUserName.editText?.text.toString())
                }
            }
        } else if (v?.id == btnSavePassword.id) {
            mAuth!!.currentUser!!.updatePassword(tfNewPassword.editText?.text.toString())
                .addOnSuccessListener {
                    tfCurrentPassword.editText?.text = null
                    tfNewPassword.editText?.text = null
                    tfConfirmPassword.editText?.text = null
                    Toast.makeText(
                        activity,
                        "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        activity,
                        "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else if (v?.id == btnLogout.id) {
            credential.clearLoggedInUser(requireActivity())
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }
    }

}