package com.papbl.drophereclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.papbl.drophereclone.utils.RealPathUtil
import java.io.File
import java.util.*


class SubmissionActivity : AppCompatActivity(), View.OnClickListener {

    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")
    private val storageRef = Firebase.storage.reference

    private lateinit var query: Task<QuerySnapshot>
    private lateinit var submissionTimeValue: MaterialTextView
    private lateinit var submissionDescription: MaterialTextView
    private lateinit var submissionUpload: MaterialButton
    private lateinit var currentPages: Pages
    private lateinit var currentDocumentId: String
    private lateinit var fileToUpload: Uri

    private var uniqueCode: String? = null
    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val UNIQUE_CODE_EXTRA = "unique_code"

        private const val PICK_PDF_REQUEST = 101
        private const val READ_EXT_STORAGE_PERMISSION = 211
    }

    data class Pages(
        val unique_code: String,
        val title: String,
        val deadline: Long,
        val ownerId: String,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)

        checkStoragePermission()

        submissionTimeValue = findViewById(R.id.tv_submission_time_value)
        submissionDescription = findViewById(R.id.tv_submission_description)
        submissionUpload = findViewById(R.id.btn_upload_file)

        submissionUpload.isEnabled = false

        uniqueCode = intent.getStringExtra(UNIQUE_CODE_EXTRA)

        query = pageCollection
            .whereEqualTo("unique_code", uniqueCode)
            .limit(1)
            .get()

        submissionUpload.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        loadPageSubmission { isSuccess ->
            if (isSuccess) {
                val countDown = currentPages.deadline - System.currentTimeMillis()
                submissionDescription.text = StringBuilder()
                    .append(currentPages.title)
                    .append("\n")
                    .append(currentPages.description)
                submissionUpload.isEnabled = true
                countDownTimer = startCountDown(countDown)
                countDownTimer?.start()
            } else {
                toastMessage(resources.getString(R.string.toast_submission_load_failed))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelCountDown()
    }

    override fun onClick(v: View?) {
        if (v?.id == submissionUpload.id) {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "application/pdf"
            }
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    resources.getString(R.string.intent_chooser_submission_upload_file)
                ), PICK_PDF_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            fileToUpload = convertFileUriToUpload(data.data)
            showDialogUploadConfirm(data.data!!.lastPathSegment.toString())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXT_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish()
            }
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXT_STORAGE_PERMISSION
            )
        }
    }

    private fun showDialogUploadConfirm(filename: String) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(resources.getString(R.string.dialog_submission_title))
            setMessage(
                "${resources.getString(R.string.dialog_submission_message_prefix)} ${currentPages.title} ${resources.getString(
                    R.string.dialog_submission_message_infix
                )} $filename ${resources.getString(R.string.dialog_submission_message_suffix)} "
            )
            setPositiveButton(resources.getString(R.string.dialog_submission_confirm_positive)) { _, _ ->
                submitSubmission()
            }
            setNegativeButton(resources.getString(R.string.dialog_submission_confirm_negative)) { _, _ -> }
        }.show()
    }

    private fun loadPageSubmission(callback: (isSuccess: Boolean) -> Unit) {
        query
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val title: String = document.data["title"] as String
                    val deadline: Timestamp = document.data["deadline"] as Timestamp
                    val ownerId: String = document.data["owner_id"] as String
                    val description: String = document.data["description"] as String

                    currentDocumentId = document.id
                    currentPages = Pages(
                        uniqueCode!!,
                        title,
                        deadline.toDate().time,
                        ownerId,
                        description
                    )

                    callback.invoke(true)
                }
            }
            .addOnFailureListener {
                callback.invoke(false)
            }
    }

    private fun submitSubmission() {
        uploadFileSubmission { isSuccess ->
            if (isSuccess) {
                val submissionData = hashMapOf(
                    "fullname" to "Muhammad Wildan Aldiansyah",
                    "submit_at" to Timestamp(Date()),
                    "user_id" to "fJMfIoDQnYaXHdzVHERnG3yIh7z1",
                    "file" to "fJMfIoDQnYaXHdzVHERnG3yIh7z1.pdf"
                )

                pageCollection
                    .document(currentDocumentId)
                    .update("senders", FieldValue.arrayUnion(submissionData))
                    .addOnSuccessListener {
                        toastMessage(resources.getString(R.string.toast_submission_upload_file_success))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("SubmissionActivity", "Error updating document", e)
                    }
            } else {
                toastMessage(resources.getString(R.string.toast_submission_upload_file_failed))
            }
        }
    }

    private fun uploadFileSubmission(callback: (isSuccess: Boolean) -> Unit) {
        val storageFolder = "${currentPages.ownerId}_${currentPages.unique_code}"
        val storageFileRef = "${storageFolder}/fJMfIoDQnYaXHdzVHERnG3yIh7z1.pdf"
        val fileRef = storageRef.child(storageFileRef)

        fileRef.putFile(fileToUpload)
            .addOnSuccessListener { callback.invoke(true) }
            .addOnFailureListener { callback.invoke(false) }
    }

    private fun convertFileUriToUpload(data: Uri?): Uri {
        val uri = RealPathUtil.getRealPath(this, data!!)
        val file = File(uri.toString())
        return Uri.fromFile(file)
    }

    private fun startCountDown(
        millisInFuture: Long,
        countDownInterval: Long = 1000
    ): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val result = countDownResult(millisUntilFinished, countDownInterval)
                submissionTimeValue.text = result
            }

            override fun onFinish() {
                finish()
            }
        }
    }

    private fun cancelCountDown() {
        countDownTimer?.cancel()
    }

    private fun countDownResult(millisUntilFinished: Long, countDownInterval: Long): String {
        val format = "%02d"
        val days = millisUntilFinished / (24 * 60 * 60 * countDownInterval)
        val hours = millisUntilFinished / (60 * 60 * countDownInterval) % 24
        val minutes = millisUntilFinished / (60 * countDownInterval) % 60
        val seconds = millisUntilFinished / (countDownInterval) % 60
        var result: String = resources.getString(R.string.tv_submission_time_value)

        when {
            days > 0L -> {
                result = StringBuilder()
                    .append(String.format(format, days))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_days))
                    .append(" ")
                    .append(String.format(format, hours))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_hours))
                    .toString()
            }
            minutes <= 0L -> {
                result = StringBuilder()
                    .append(String.format(format, seconds))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_seconds))
                    .toString()
            }
            hours <= 0L -> {
                result = StringBuilder()
                    .append(String.format(format, minutes))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_minutes))
                    .append(" ")
                    .append(String.format(format, seconds))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_seconds))
                    .toString()
            }
            days <= 0L -> {
                result = StringBuilder()
                    .append(String.format(format, hours))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_hours))
                    .append(" ")
                    .append(String.format(format, minutes))
                    .append(" ")
                    .append(resources.getString(R.string.count_down_minutes))
                    .toString()
            }
        }

        return result
    }

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
