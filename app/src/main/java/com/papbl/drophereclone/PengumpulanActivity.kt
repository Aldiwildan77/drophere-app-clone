package com.papbl.drophereclone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")

    private lateinit var tvDeadline: MaterialTextView
    private lateinit var tvTitle: MaterialTextView
    private lateinit var tvUniqueCode: MaterialTextView
    private lateinit var ibUniqueCode: ImageButton
    private lateinit var ibPopupAction: ImageButton
    private  lateinit var btnUndugBerkas: MaterialButton
    private lateinit var rvFileTerkumpul: RecyclerView

    private var senderData: ArrayList<SenderData> = ArrayList()

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

        getPageDetail()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            ibUniqueCode.id -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Cek", tvUniqueCode.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copy to Clipboard", Toast.LENGTH_SHORT).show()
            }
            ibPopupAction.id -> {
                showActionMenu(v)
            }
            btnUndugBerkas.id -> {
                Toast.makeText(this, "Unduh semua berkas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getPageDetail() {
        pageCollection
            .whereEqualTo("owner_id", "21k09ascAC1kL3z2")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val deadline: Timestamp = document.data.get("deadline") as Timestamp
                    val simpleDateFormat =
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    simpleDateFormat.setTimeZone(TimeZone.getDefault())
                    tvDeadline.setText(simpleDateFormat.format(deadline.toDate()))
                    tvTitle.setText(document.data.get("title").toString())
                    tvUniqueCode.setText(document.data.get("unique_code").toString())
                    val senders = document.data.get("senders") as ArrayList<Map<Any, Any>>
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
                        adapter = PengumpulanAdapter(senderData, tvDeadline.text.toString())
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
                Toast.makeText(this, "Hapus", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onContextItemSelected(item)
    }
}
