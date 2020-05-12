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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.papbl.drophereclone.models.Credential
import com.papbl.drophereclone.utils.UserCredential


class ProfileFragment : Fragment(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    private val credential = UserCredential()

    private var fullnameCondition: Boolean = false
    private var emailAddressCondition: Boolean = false

    private lateinit var tfFullname: TextInputLayout
    private lateinit var tfEmailAddress: TextInputLayout
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

        tfFullname = view.findViewById(R.id.til_profile_name)
        tfEmailAddress = view.findViewById(R.id.til_profile_email)
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

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()

        tfFullname.editText?.setText(credential.getLoggedUser(requireContext()).fullname)
        tfEmailAddress.editText?.setText(credential.getLoggedUser(requireActivity()).email)

        tfFullname.editText?.addTextChangedListener(fullNameWatcher)
        tfEmailAddress.editText?.addTextChangedListener(emailWatcher)
        tfNewPassword.editText?.addTextChangedListener(newPasswordWatcher)
        tfCurrentPassword.editText?.addTextChangedListener(currentPasswordWatcher)
        tfConfirmPassword.editText?.addTextChangedListener(confirmPasswordWatcher)
    }

    private val newPasswordWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }
    }

    private val currentPasswordWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }
    }

    private val confirmPasswordWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            btnSavePassword.isEnabled = validateSubmitPassword()
        }
    }

    private val fullNameWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            validateFullname(s.toString())
            validateEmail(tfEmailAddress.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validateFullname(s.toString())
            validateEmail(tfEmailAddress.editText?.text.toString())
        }
    }

    private val emailWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            validateEmail(s.toString())
            validateFullname(tfFullname.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validateEmail(s.toString())
            validateFullname(tfFullname.editText?.text.toString())
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
            if (fullnameCondition) {
                getData { _, id ->
                    userCollection
                        .document(id)
                        .update("fullname", tfFullname.editText?.text.toString())
                }
                toastMessage(resources.getString(R.string.toast_profile_fullname_change_success))
                updateUser()
            } else if (emailAddressCondition) {
                mAuth!!.currentUser!!.updateEmail(tfEmailAddress.editText?.text.toString())
                    .addOnSuccessListener {
                        toastMessage(resources.getString(R.string.toast_profile_email_change_success))
                        updateUser()
                    }
                    .addOnFailureListener {
                        toastMessage(resources.getString(R.string.toast_profile_email_change_failed))
                    }
            }
        } else if (v?.id == btnSavePassword.id) {

            val currentCredential = EmailAuthProvider.getCredential(
                credential.getLoggedUser(requireActivity()).email,
                tfCurrentPassword.editText?.text.toString()
            )

            mAuth!!.currentUser!!.reauthenticateAndRetrieveData(currentCredential)
                .addOnSuccessListener {
                    mAuth!!.currentUser!!.updatePassword(tfNewPassword.editText?.text.toString())
                        .addOnSuccessListener {
                            tfCurrentPassword.editText?.text = null
                            tfNewPassword.editText?.text = null
                            tfConfirmPassword.editText?.text = null
                            toastMessage(resources.getString(R.string.toast_profile_password_change_success))
                        }
                        .addOnFailureListener {
                            toastMessage(resources.getString(R.string.toast_profile_password_change_failed))
                        }
                }.addOnFailureListener {
                    toastMessage(resources.getString(R.string.toast_profile_password_is_wrong))
                }
        } else if (v?.id == btnLogout.id) {
            if (credential.clearLoggedInUser(requireActivity())) {
                mAuth!!.signOut()
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            }
        }
    }

    private fun updateUser() {
        credential.updateLoggedUser(
            requireActivity(),
            Credential(
                tfEmailAddress.editText?.text.toString(),
                credential.getLoggedUser(requireActivity()).uid,
                tfFullname.editText?.text.toString()
            )
        )
    }

    private fun validateFullname(fullname: String) {
        fullnameCondition = fullname != credential.getLoggedUser(requireActivity()).fullname
                && fullname.isNotEmpty()
        validateSubmitProfile()
    }

    private fun validateEmail(email: String) {
        emailAddressCondition = (isEmailValid(email)
                && email != credential.getLoggedUser(requireActivity()).email
                && email.isNotEmpty())
        validateSubmitProfile()
    }

    private fun validateSubmitProfile() {
        val validateAll = when {
            emailAddressCondition && fullnameCondition -> {
                emailAddressCondition && fullnameCondition
            }
            emailAddressCondition -> {
                emailAddressCondition
            }
            else -> {
                fullnameCondition
            }
        }

        btnSaveProfile.isEnabled = validateAll
    }

    private fun validateSubmitPassword(): Boolean {
        return tfNewPassword.editText?.text!!.isNotEmpty()
                && tfNewPassword.editText?.text!!.length >= 8
                && tfCurrentPassword.editText?.text!!.isNotEmpty()
                && tfCurrentPassword.editText?.text!!.length >= 8
                && tfConfirmPassword.editText?.text!!.isNotEmpty()
                && tfConfirmPassword.editText?.text!!.length >= 8
                && tfConfirmPassword.editText?.text.toString() == tfNewPassword.editText?.text.toString()
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun toastMessage(message: String) {
        Toast.makeText(
            activity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}