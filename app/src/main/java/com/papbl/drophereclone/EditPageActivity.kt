package com.papbl.drophereclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.papbl.drophereclone.utils.UserCredential
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class EditPageActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    CompoundButton.OnCheckedChangeListener {
    private val credential = UserCredential()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")
    private lateinit var query: Task<QuerySnapshot>
    private lateinit var pageData: PageData

    private lateinit var filterUniqueCode: String
    private lateinit var filterOwnerId: String

    private lateinit var passwordSwitch: SwitchMaterial
    private lateinit var deadlineSwitch: SwitchMaterial

    private lateinit var pagePassword: TextInputLayout
    private lateinit var pageTitle: TextInputLayout
    private lateinit var pageDescription: TextInputLayout
    private lateinit var pageDeadline: TextInputLayout

    private lateinit var pageSubmit: MaterialButton

    private var isTitleValid: Boolean = false
    private var isPasswordValid: Boolean = false
    private var isDeadlineValid: Boolean = false
    private var dateTimeSelected: String = ""

    private val deadline = Calendar.getInstance()

    companion object {
        private const val KEY_EXTRA_UNIQUE_CODE: String = "unique_code"
    }

    data class PageData(
        val unique_code: String,
        val ownerId: String,
        var title: String,
        var description: String,
        val deleted: Boolean = false,
        var deadline: Timestamp? = null,
        var password: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_page)

        filterUniqueCode = intent.extras?.get(KEY_EXTRA_UNIQUE_CODE) as String
        filterOwnerId = credential.getLoggedUser(this).uid

        passwordSwitch = findViewById(R.id.sw_password)
        deadlineSwitch = findViewById(R.id.sw_deadline)

        pagePassword = findViewById(R.id.til_page_password)
        pageTitle = findViewById(R.id.til_page_title)
        pageDescription = findViewById(R.id.til_page_description)
        pageDeadline = findViewById(R.id.til_page_deadline)

        pageSubmit = findViewById(R.id.btn_page_create_submit)

        passwordSwitch.setOnCheckedChangeListener(this)
        deadlineSwitch.setOnCheckedChangeListener(this)

        pageTitle.editText?.addTextChangedListener(titleWatcher)
        pagePassword.editText?.addTextChangedListener(passwordWatcher)
        pageDeadline.editText?.addTextChangedListener(deadlineWatcher)

        pageDeadline.editText?.setOnClickListener(this)
        pageSubmit.setOnClickListener(this)

        pageSubmit.isEnabled = true

        query = pageCollection
            .whereEqualTo("ownerId", filterOwnerId)
            .whereEqualTo("unique_code", filterUniqueCode)
            .whereEqualTo("deleted", false)
            .get()

        getPageDetail()
    }

    private fun getPageDetail() {
        query.addOnSuccessListener { documents ->
            for (document in documents) {
                pageData = PageData(
                    document.data["unique_code"].toString(),
                    document.data["ownerId"].toString(),
                    document.data["title"].toString(),
                    document.data["description"].toString(),
                    document.data["deleted"] as Boolean,
                    document.data["deadline"] as? Timestamp,
                    document.data["password"].toString()
                )
            }
            if (pageData.password != "null") {
                passwordSwitch.isChecked = true
                pagePassword.editText?.setText(pageData.password)
            } else {
                pagePassword.apply {
                    isEnabled = false
                    if (!isEnabled) {
                        boxBackgroundColor = ContextCompat.getColor(
                            this@EditPageActivity,
                            R.color.colorFieldDisabled
                        )
                    }
                }
            }
            if (pageData.deadline != null) {
                deadlineSwitch.isChecked = true
                val simpleDateFormat =
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
                simpleDateFormat.timeZone = TimeZone.getDefault()
                val deadlineString = simpleDateFormat.format(pageData.deadline!!.toDate())
                pageDeadline.editText?.setText(deadlineString)
            } else {
                pageDeadline.apply {
                    isEnabled = false
                    if (!isEnabled) {
                        boxBackgroundColor = ContextCompat.getColor(
                            this@EditPageActivity,
                            R.color.colorFieldDisabled
                        )
                    }
                }
            }
            pageTitle.editText?.setText(pageData.title)
            pageDescription.editText?.setText(pageData.description)
        }
    }

    private fun updatePageDetail() {
        val password =
            if (pagePassword.isEnabled) pagePassword.editText?.text.toString() else null
        val title = pageTitle.editText?.text.toString()
        val description = pageDescription.editText?.text.toString()
        val deadline =
            if (pageDeadline.isEnabled) convertDeadlineToDate(pageDeadline.editText?.text.toString()) else null
        query.addOnSuccessListener {
            val documentId = it.documents[0].id
            pageCollection.document(documentId).update(
                mutableMapOf(
                    "password" to password,
                    "title" to title,
                    "description" to description,
                    "deadline" to deadline
                ) as Map<String, Any>
            )
        }
    }

    private fun convertDeadlineToDate(deadline: String): Date {
        val formatPattern = "dd/MM/yyyy HH:mm:ss"
        val dateFormat = SimpleDateFormat(formatPattern, Locale("id", "ID"))
        return dateFormat.parse(deadline)!!
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            pageSubmit.id -> {
                updatePageDetail()
                Toast.makeText(this, "Informasi Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            pageDeadline.editText?.id -> {
                createDatePickerDialog().show(
                    supportFragmentManager,
                    createDatePickerDialog().toString()
                )
            }
        }
    }

    private fun createDatePickerDialog(): DatePickerDialog {
        return DatePickerDialog.newInstance(
            this,
            deadline[Calendar.YEAR],
            deadline[Calendar.MONTH],
            deadline[Calendar.DAY_OF_MONTH]
        ).apply {
            version = DatePickerDialog.Version.VERSION_2
            accentColor = ContextCompat.getColor(this@EditPageActivity, R.color.colorAccent)
            isCancelable = false
            minDate = deadline
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        dateTimeSelected = "$dayOfMonth/${monthOfYear + 1}/$year"
        createTimePickerDialog().show(
            supportFragmentManager,
            createTimePickerDialog().toString()
        )
    }

    private fun createTimePickerDialog(): TimePickerDialog {
        return TimePickerDialog.newInstance(
            this,
            deadline[Calendar.HOUR_OF_DAY],
            deadline[Calendar.MINUTE],
            deadline[Calendar.SECOND],
            true
        ).apply {
            version = TimePickerDialog.Version.VERSION_2
            accentColor = ContextCompat.getColor(this@EditPageActivity, R.color.colorAccent)
            isCancelable = false
            enableSeconds(true)
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        dateTimeSelected += " $hourOfDay:$minute:$second"
        pageDeadline.editText?.setText(dateTimeSelected, TextView.BufferType.EDITABLE)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            passwordSwitch.id -> {
                validatePassword(pagePassword.editText?.text.toString())
                pagePassword.apply {
                    isEnabled = isChecked
                    boxBackgroundColor = if (isChecked) 0 else ContextCompat.getColor(
                        this@EditPageActivity,
                        R.color.colorFieldDisabled
                    )
                    error = null
                    editText?.apply {
                        isEnabled = isChecked
                        text = null
                        inputType =
                            if (isChecked) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_NULL
                    }
                }
                validationSubmitButton()
            }
            deadlineSwitch.id -> {
                dateTimeSelected = ""
                validateDeadline(dateTimeSelected)
                pageDeadline.apply {
                    isEnabled = isChecked
                    boxBackgroundColor = if (isChecked) 0 else ContextCompat.getColor(
                        this@EditPageActivity,
                        R.color.colorFieldDisabled
                    )
                    error = null
                    editText?.apply {
                        isEnabled = isChecked
                        text = null
                        inputType =
                            if (isChecked) InputType.TYPE_CLASS_DATETIME else InputType.TYPE_NULL
                    }
                }
                validationSubmitButton()
            }
        }
    }

    private val titleWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            validateTitle(pageTitle.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            validateTitle(pageTitle.editText?.text.toString())
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validateTitle(pageTitle.editText?.text.toString())
        }
    }

    private val passwordWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (pagePassword.isEnabled) validatePassword(pagePassword.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (pagePassword.isEnabled) validatePassword(pagePassword.editText?.text.toString())
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (pagePassword.isEnabled) validatePassword(pagePassword.editText?.text.toString())
        }
    }

    private val deadlineWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (pageDeadline.isEnabled) validateDeadline(pageDeadline.editText?.text.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (pageDeadline.isEnabled) validateDeadline(pageDeadline.editText?.text.toString())
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (pageDeadline.isEnabled) validateDeadline(pageDeadline.editText?.text.toString())
        }
    }

    private fun validationSubmitButton() {
        val validationAll = when {
            pageDeadline.isEnabled && pagePassword.isEnabled -> {
                isDeadlineValid && isPasswordValid && isTitleValid
            }
            pageDeadline.isEnabled -> {
                isDeadlineValid && isTitleValid
            }
            pagePassword.isEnabled -> {
                isPasswordValid && isTitleValid
            }
            else -> {
                isTitleValid
            }
        }
        pageSubmit.isEnabled = validationAll
    }

    private fun validatePassword(password: String = "") {
        pagePassword.error =
            if (password.isEmpty()) resources.getString(R.string.helper_page_empty_password) else null
        isPasswordValid = password.isNotEmpty()
        validationSubmitButton()
    }

    private fun validateTitle(title: String = "") {
        pageTitle.error =
            if (title.isEmpty()) resources.getString(R.string.helper_page_empty_title) else null
        isTitleValid = title.isNotEmpty()
        validationSubmitButton()
    }

    private fun validateDeadline(deadline: String = "") {
        if (deadline.isEmpty()) {
            pageDeadline.error = resources.getString(R.string.helper_page_empty_deadline)
            isDeadlineValid = false
        } else {
            val convertedDate = convertDeadlineToDate(deadline)
            val result = convertedDate.time - Date().time
            pageDeadline.error =
                if (result <= 0L) resources.getString(R.string.helper_page_more_than_deadline) else null
            isDeadlineValid = result > 0L
        }
        validationSubmitButton()
    }
}
