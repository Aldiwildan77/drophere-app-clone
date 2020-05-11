package com.papbl.drophereclone

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.papbl.drophereclone.utils.UserCredential
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

data class SenderData(
    val fileName: String,
    val fullName: String,
    val submitAt: Timestamp,
    val userId: String
)

class PengumpulanActivity : AppCompatActivity(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {
    private val credential = UserCredential()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")
    private val storageRef = Firebase.storage.reference
    private lateinit var query: Task<QuerySnapshot>

    private lateinit var tvDeadline: MaterialTextView
    private lateinit var tvTitle: MaterialTextView
    private lateinit var tvUniqueCode: MaterialTextView
    private lateinit var ibUniqueCode: ImageButton
    private lateinit var ibPopupAction: ImageButton
    private lateinit var btnSenderInfo: ImageButton
    private lateinit var btnDownloadFile: MaterialButton
    private lateinit var rvFileSubmitted: RecyclerView

    private lateinit var senders: ArrayList<Map<Any?, Any?>>
    private var senderData: ArrayList<SenderData> = ArrayList()
    private var roomPassword: String? = null

    private lateinit var filterUniqueCode: String
    private lateinit var filterOwnerId: String
    private lateinit var ownerId: String
    private lateinit var uniqueCode: String

    companion object {
        private const val KEY_EXTRA_UNIQUE_CODE: String = "unique_code"
        private const val WRITE_EXT_STORAGE_PERMISSION = 423
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengumpulan)

        filterUniqueCode = intent.extras?.get(KEY_EXTRA_UNIQUE_CODE) as String
        filterOwnerId = credential.getLoggedUser(this).uid

        checkStoragePermission()

        tvDeadline = findViewById(R.id.tv_deadline)
        tvTitle = findViewById(R.id.tv_card_title)
        tvUniqueCode = findViewById(R.id.tv_unique_code_value)
        ibUniqueCode = findViewById(R.id.ib_unique_code_copy)
        ibPopupAction = findViewById(R.id.ib_more_popup)
        btnDownloadFile = findViewById(R.id.btn_unduh_berkas)
        btnSenderInfo = findViewById(R.id.ib_more_sender_popup)
        rvFileSubmitted = findViewById(R.id.rv_file_terkumpul)

        ibUniqueCode.setOnClickListener(this)
        ibPopupAction.setOnClickListener(this)
        btnDownloadFile.setOnClickListener(this)
        btnSenderInfo.setOnClickListener(this)

        query = pageCollection
            .whereEqualTo("ownerId", filterOwnerId)
            .whereEqualTo("unique_code", filterUniqueCode)
            .whereEqualTo("deleted", false)
            .get()

        getPageDetail()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            ibUniqueCode.id -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Kode Unik", tvUniqueCode.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Kode unik berhasil disalin!", Toast.LENGTH_SHORT).show()
            }
            ibPopupAction.id -> {
                showActionMenu(v)
            }
            btnDownloadFile.id -> {
                val folderRoot = File("/storage/emulated/0/Drop Here/")
                if (!folderRoot.exists()) folderRoot.mkdir()
                val folderDir =
                    File(folderRoot, uniqueCode + "_" + tvTitle.text.toString())
                folderDir.mkdir()
                val folderRef = storageRef.child(ownerId + "_" + uniqueCode)
                if (rvFileSubmitted.adapter?.itemCount != 0) {
                    MaterialAlertDialogBuilder(this).apply {
                        setTitle("Unduh Berkas")
                        setMessage(
                            "Apa anda yakin ingin mengunduh berkas pengumpulan ini?"
                        )
                        setPositiveButton("Ya") { _, _ ->
                            downloadAllFile(folderDir, folderRef)
                        }
                        setNegativeButton("Tidak") { _, _ -> }
                    }.show()
                } else {
                    Toast.makeText(this, "Belum ada yang mengumpulkan tugas", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            btnSenderInfo.id -> {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Lokasi Penyimpanan Berkas")
                    setMessage("/storage/emulated/0/Drop Here/")
                    setPositiveButton(resources.getString(R.string.dialog_submission_confirm_positive)) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXT_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish()
            }
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXT_STORAGE_PERMISSION
            )
        }
    }

    private fun getPageDetail() {
        query.addOnSuccessListener { documents ->
            for (document in documents) {
                val deadline: Timestamp? = document.data["deadline"] as? Timestamp
                val simpleDateFormat =
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                simpleDateFormat.timeZone = TimeZone.getDefault()
                ownerId = document.data["ownerId"].toString()
                uniqueCode = document.data["unique_code"].toString()
                tvDeadline.text =
                    if (deadline == null) {
                        resources.getString(R.string.card_pages_tv_no_due_date)
                    } else {
                        simpleDateFormat.format(deadline.toDate())
                    }
                roomPassword =
                    if (document.data["password"].toString() != "null") document.data["password"].toString() else null
                tvTitle.text = document.data["title"].toString()
                tvUniqueCode.text = (uniqueCode)
                if (document.data["senders"] != null) {
                    senders = document.data["senders"] as ArrayList<Map<Any?, Any?>>
                    senders.sortWith(Comparator { o1, o2 ->
                        val data1 = o1?.get("submit_at") as Timestamp
                        val data2 = o2?.get("submit_at") as Timestamp
                        data2.compareTo(data1)
                    })
                    senders.forEach {
                        val fileName = it["file"].toString()
                        val fullName = it["fullname"].toString()
                        val submitAt = it["submit_at"] as Timestamp
                        val userId = it["user_id"].toString()
                        val sender = SenderData(fileName, fullName, submitAt, userId)
                        senderData.add(sender)
                    }
                }
                rvFileSubmitted.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = PengumpulanAdapter(
                        senderData,
                        tvDeadline.text.toString(),
                        ownerId,
                        uniqueCode,
                        tvTitle.text.toString()
                    )
                }
            }
        }
    }

    private fun downloadAllFile(folderDir: File?, folderRef: StorageReference) {
        folderRef.listAll().addOnSuccessListener {
            val total = it.items.size
            var current = 0
            it.items.forEachIndexed { index, fileRef ->
                val fileName = senderData[index].fullName + "_" + senderData[index].fileName
                val file = File(folderDir, fileName)
                fileRef.getFile(file).addOnSuccessListener {
                    current += 1
                    Toast.makeText(
                        this,
                        "Berhasil mengunduh file (${current}/${total})",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { error ->
                    Log.e("PengumpulanActivity", error.message.toString())
                }
            }
        }
    }

    private fun showActionMenu(v: View) {
        PopupMenu(this, v).apply {
            inflate(R.menu.menu_action_pengumpulan)
            setOnMenuItemClickListener { item -> onMenuItemClick(item) }
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_show_password -> {
                if (roomPassword.isNullOrEmpty()) {
                    Toast.makeText(this, "Tidak ada password halaman", Toast.LENGTH_SHORT).show()
                } else {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("Password Pengumpulan", roomPassword)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Password berhasil disalin!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_edit -> {
                Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_hapus -> {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Hapus Pengumpulan")
                    setMessage(
                        "Apa anda yakin ingin menghapus pengumpulan ini?"
                    )
                    setPositiveButton("Ya") { _, _ ->
                        query.addOnSuccessListener {
                            val documentId = it.documents[0].id
                            pageCollection.document(documentId).update("deleted", true)
                        }
                        Toast.makeText(context, "Pengumpulan berhasil dihapus", Toast.LENGTH_SHORT)
                            .show()
                        setResult(Activity.RESULT_OK, Intent())
                        finish()
                    }
                    setNegativeButton("Tidak") { _, _ -> }
                }.show()
            }
        }
        return super.onContextItemSelected(item)
    }
}
