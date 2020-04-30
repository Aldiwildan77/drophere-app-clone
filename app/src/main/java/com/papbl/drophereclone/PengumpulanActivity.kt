package com.papbl.drophereclone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private var fileNames: ArrayList<String> = ArrayList()

    private lateinit var ownerId: String
    private lateinit var uniqueCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengumpulan)

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
            .whereEqualTo("owner_id", "21k09ascAC1kL3z2")
            .whereEqualTo("unique_code", "51k01ap")
            .whereEqualTo("is_deleted", false)
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
                val fileDir =
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + ownerId + "_" + uniqueCode)
                val folderRef = storageRef.child(ownerId + "_" + uniqueCode)
                downloadAllFile(fileDir, folderRef)
            }
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
                    fileNames.add(fileName)
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

    fun downloadAllFile(fileDir: File?, folderRef: StorageReference) {
        folderRef.listAll().addOnSuccessListener {
            val total = it.items.size
            var current = 0
            it.items.forEachIndexed { index, fileRef ->
                val fileName = fileNames.get(index)
                val file = File(fileDir, fileName)
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
                query.addOnSuccessListener {
                    val documentId = it.documents.get(0).id
                    pageCollection.document(documentId).update("is_deleted", true)
                }
                Toast.makeText(this, "Pengumpulan berhasil dihapus", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        return super.onContextItemSelected(item)
    }
}
