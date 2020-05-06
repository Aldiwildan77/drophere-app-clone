package com.papbl.drophereclone

import android.Manifest
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
    //    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")
    private val storageRef = Firebase.storage.reference
    private lateinit var query: Task<QuerySnapshot>

    private lateinit var tvDeadline: MaterialTextView
    private lateinit var tvTitle: MaterialTextView
    private lateinit var tvUniqueCode: MaterialTextView
    private lateinit var ibUniqueCode: ImageButton
    private lateinit var ibPopupAction: ImageButton
    private lateinit var btnUndugBerkas: MaterialButton
    private lateinit var rvFileTerkumpul: RecyclerView

    private lateinit var senders: ArrayList<Map<Any, Any>>
    private var senderData: ArrayList<SenderData> = ArrayList()

    private lateinit var ownerId: String
    private lateinit var uniqueCode: String

    private val WRITE_EXT_STORAGE_PERMISSION = 423

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengumpulan)

        checkStoragePermission()

        tvDeadline = findViewById(R.id.tv_deadline)
        tvTitle = findViewById(R.id.tv_card_title)
        tvUniqueCode = findViewById(R.id.tv_unique_code_value)
        ibUniqueCode = findViewById(R.id.ib_unique_code_copy)
        ibPopupAction = findViewById(R.id.ib_more_popup)
        btnUndugBerkas = findViewById(R.id.btn_unduh_berkas)
        rvFileTerkumpul = findViewById(R.id.rv_file_terkumpul)

        ibUniqueCode.setOnClickListener(this)
        ibPopupAction.setOnClickListener(this)
        btnUndugBerkas.setOnClickListener(this)

        query = pageCollection
            .whereEqualTo("owner_id", "21k09ascAC1kL3z1")
            .whereEqualTo("unique_code", "51k01ap")
            .whereEqualTo("deleted", false)
            .get()

        getPageDetail()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            ibUniqueCode.id -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Cek", tvUniqueCode.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Kode unik berhasil disalin", Toast.LENGTH_SHORT).show()
            }
            ibPopupAction.id -> {
                showActionMenu(v)
            }
            btnUndugBerkas.id -> {
                val folderRoot = File("/storage/emulated/0/Drop Here/")
                if (!folderRoot.exists()) folderRoot.mkdir()
                val folderDir = File(folderRoot, ownerId + "_" + uniqueCode)
                folderDir.mkdir()
                val folderRef = storageRef.child(ownerId + "_" + uniqueCode)
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

    fun getPageDetail() {
        query.addOnSuccessListener { documents ->
            for (document in documents) {
                val deadline: Timestamp = document.data.get("deadline") as Timestamp
                val simpleDateFormat =
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                simpleDateFormat.setTimeZone(TimeZone.getDefault())
                ownerId = document.data.get("owner_id").toString()
                uniqueCode = document.data.get("unique_code").toString()
                tvDeadline.setText(simpleDateFormat.format(deadline.toDate()))
                tvTitle.setText(document.data.get("title").toString())
                tvUniqueCode.setText(uniqueCode)
                senders = document.data.get("senders") as ArrayList<Map<Any, Any>>
                Collections.sort(senders, object : Comparator<Map<Any, Any>> {
                    override fun compare(obj1: Map<Any, Any>, obj2: Map<Any, Any>): Int {
                        val data1 = obj1.get("submit_at") as Timestamp
                        val data2 = obj2.get("submit_at") as Timestamp
                        return data2.compareTo(data1)
                    }
                })
                senders.forEach {
                    val fileName = it.get("file").toString()
                    val fullName = it.get("fullname").toString()
                    val submitAt = it.get("submit_at") as Timestamp
                    val userId = it.get("user_id").toString()
                    val sender = SenderData(fileName, fullName, submitAt, userId)
                    senderData.add(sender)
                }
                rvFileTerkumpul.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = PengumpulanAdapter(
                        senderData,
                        tvDeadline.text.toString(),
                        ownerId,
                        uniqueCode
                    )
                }
            }
        }
    }

    fun downloadAllFile(folderDir: File?, folderRef: StorageReference) {
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
                }.addOnFailureListener {
                    Log.e("PengumpulanActivity", it.message.toString())
                }
            }
        }
    }

    fun showActionMenu(v: View) {
        PopupMenu(this, v).apply {
            inflate(R.menu.menu_action_pengumpulan)
            setOnMenuItemClickListener { item -> onMenuItemClick(item) }
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
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
                            val documentId = it.documents.get(0).id
                            pageCollection.document(documentId).update("deleted", true)
                        }
                        Toast.makeText(context, "Pengumpulan berhasil dihapus", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(context, MainActivity::class.java))
                    }
                    setNegativeButton("Tidak") { _, _ -> }
                }.show()
            }
        }
        return super.onContextItemSelected(item)
    }
}
