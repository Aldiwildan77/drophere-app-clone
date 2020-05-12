package com.papbl.drophereclone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.papbl.drophereclone.utils.UserCredential
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.*


class CreatePageActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private val credential = UserCredential()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")

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

    data class Pages(
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
        setContentView(R.layout.activity_create_page)

        bindView()
        bindEvent()
    }

    override fun onClick(v: View?) {
        if (v?.id == pageSubmit.id && isTitleValid) {
            val createPagesData = Pages(
                generateUniqueCode(),
                credential.getLoggedUser(this).uid,
                pageTitle.editText?.text.toString(),
                pageDescription.editText?.text.toString()
            )

            if (passwordSwitch.isEnabled && isPasswordValid) {
                createPagesData.password = pagePassword.editText?.text.toString()
            }

            if (pageDeadline.isEnabled && isDeadlineValid) {
                createPagesData.deadline = Timestamp(convertDeadlineToDate(dateTimeSelected))
            }

            createNewPage(createPagesData)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            passwordSwitch.id -> {
                validatePassword(pagePassword.editText?.text.toString())
                pagePassword.apply {
                    isEnabled = isChecked
                    boxBackgroundColor = if (isChecked) 0 else ContextCompat.getColor(
                        this@CreatePageActivity,
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
                        this@CreatePageActivity,
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

    private fun createNewPage(pages: Pages) {
        pageCollection.add(pages)
            .addOnSuccessListener {
                setResult(Activity.RESULT_OK, Intent())
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, resources.getString(R.string.toast_page_something_wrong), Toast.LENGTH_SHORT).show()
                Log.w("SubmissionActivity", "Error updating document", e)
            }
    }

    private fun bindView() {
        passwordSwitch = findViewById(R.id.sw_password)
        deadlineSwitch = findViewById(R.id.sw_deadline)

        pagePassword = findViewById(R.id.til_page_password)
        pageTitle = findViewById(R.id.til_page_title)
        pageDescription = findViewById(R.id.til_page_description)
        pageDeadline = findViewById(R.id.til_page_deadline)

        pageSubmit = findViewById(R.id.btn_page_create_submit)

        pagePassword.apply {
            isEnabled = false
            boxBackgroundColor = ContextCompat.getColor(
                this@CreatePageActivity,
                R.color.colorFieldDisabled
            )
        }
        pageDeadline.apply {
            isEnabled = false
            boxBackgroundColor = ContextCompat.getColor(
                this@CreatePageActivity,
                R.color.colorFieldDisabled
            )
        }
    }

    private fun bindEvent() {
        passwordSwitch.setOnCheckedChangeListener(this)
        deadlineSwitch.setOnCheckedChangeListener(this)

        pageSubmit.setOnClickListener(this)

        pageDeadline.editText?.setOnClickListener(this)

        pageTitle.editText?.addTextChangedListener(titleWatcher)
        pagePassword.editText?.addTextChangedListener(passwordWatcher)
        pageDeadline.editText?.addTextChangedListener(deadlineWatcher)

        deadlineFromDateTimePicker()
    }

    private fun deadlineFromDateTimePicker() {
        pageDeadline.editText?.onEndDrawableClicked {
            createDatePickerDialog().show(
                supportFragmentManager,
                createDatePickerDialog().toString()
            )
        }
    }

    private fun generateUniqueCode(): String {
        val lengthOfUniqueCode = 7
        val sources: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(lengthOfUniqueCode) { sources.random() }.joinToString("")
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
            if (pageDeadline.isEnabled) validateDeadline(dateTimeSelected)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (pageDeadline.isEnabled) validateDeadline(dateTimeSelected)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (pageDeadline.isEnabled) validateDeadline(dateTimeSelected)
        }
    }

    private fun validateTitle(title: String = "") {
        pageTitle.error =
            if (title.isEmpty()) resources.getString(R.string.helper_page_empty_title) else null
        isTitleValid = title.isNotEmpty()
        validationSubmitButton()
    }

    private fun validatePassword(password: String = "") {
        pagePassword.error =
            if (password.isEmpty()) resources.getString(R.string.helper_page_empty_password) else null
        isPasswordValid = password.isNotEmpty()
        validationSubmitButton()
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDeadlineToDate(deadline: String): Date {
        val formatPattern = "dd/MM/yyyy HH:mm:ss"
        val dateFormat = SimpleDateFormat(formatPattern)
        return dateFormat.parse(deadline)!!
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

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onEndDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
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
            accentColor = ContextCompat.getColor(this@CreatePageActivity, R.color.colorAccent)
            isCancelable = false
            enableSeconds(true)
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        dateTimeSelected += " $hourOfDay:$minute:$second"
        pageDeadline.editText?.setText(dateTimeSelected, TextView.BufferType.EDITABLE)
    }

    private fun createDatePickerDialog(): DatePickerDialog {
        return DatePickerDialog.newInstance(
            this,
            deadline[Calendar.YEAR],
            deadline[Calendar.MONTH],
            deadline[Calendar.DAY_OF_MONTH]
        ).apply {
            version = DatePickerDialog.Version.VERSION_2
            accentColor = ContextCompat.getColor(this@CreatePageActivity, R.color.colorAccent)
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

}
