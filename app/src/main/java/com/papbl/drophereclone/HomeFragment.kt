package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val listPage: ArrayList<ItemPage> = arrayListOf()
    private lateinit var tvCodeFind: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword("aliefazuka123@gmail.com", "filkom123")
        showRecyclerList()
        btn_kumpulkan_file.setOnClickListener {
            val dialogView: View = layoutInflater.inflate(R.layout.input_unique_code, null)
            val textUniqueCode: TextInputEditText = dialogView.findViewById(R.id.tv_search_unique)
            MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.title_dialog_home))
                .setView(dialogView)
                .setPositiveButton(resources.getString(R.string.submit)) { dialog, which ->
                    val uniqueCode = textUniqueCode.text.toString()
                    findPage(uniqueCode)
                }
                .show()
        }
    }

    private fun findPage(uniqueCode: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pages")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.getString("unique_code") == uniqueCode) {
                        val page = ItemPage(
                            document.getTimestamp("deadline"),
                            document.getBoolean("deleted")!!,
                            document.getString("description"),
                            document.getString("ownerId"),
                            document.getString("password"),
                            document.getString("title"),
                            document.getString("unique_code")
                        )
                        if (document.getString("password") != null) {
//                                Log.d("ada sandi", "oke")
                            inputPasswordCheckDialog(page)
                            break
                        } else {
//                                Log.d("tidak ada sandi", "oke")
                            val intentTest = Intent(activity, TestActivity::class.java)
                            intentTest.putExtra("extra_page", page)
                            startActivity(intentTest)
                        }

                    } else {
                        Log.d("unique code salah", "oke")
//                           Toast.makeText(context, getString(R.string.wrong_unique_code), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun inputPasswordCheckDialog(page: ItemPage) {
        val dialogView: View = layoutInflater.inflate(R.layout.text_input_password, null)
        val textPassword: TextInputEditText = dialogView.findViewById(R.id.tv_password_check)
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.input_password_text))
            .setPositiveButton(resources.getString(R.string.submit)) { dialog, which ->
                val textInputPassword = textPassword.text.toString()
                if (textInputPassword == page.password) {
//                    Log.d("sandi benar", "oke")
                    val intentTest = Intent(activity, TestActivity::class.java)
                    intentTest.putExtra("extra_page", page)
                    startActivity(intentTest)
                } else {
//                    Log.d("sandi salah", "oke")
                    Toast.makeText(context, getString(R.string.wrong_password), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setView(dialogView)
            .show()
    }

    private fun showRecyclerList() {
        getPageList { item: ArrayList<ItemPage> ->
            pb_load_list_page.visibility = View.GONE
            listPage.addAll(item)
            rv_page.layoutManager = LinearLayoutManager(activity)
            val listPageAdapter = ManagePageAdapter(listPage)
            rv_page.adapter = listPageAdapter
            listPageAdapter.notifyDataSetChanged()
        }
    }

    private fun getPageList(callback: (item: ArrayList<ItemPage>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pages")
            .get()
            .addOnSuccessListener { result ->
                val listPage: ArrayList<ItemPage> = arrayListOf()
                for (document in result) {
                    if (document.getString("ownerId") == "fJMfIoDQnYaXHdzVHERnG3yIh7z1") {
                        listPage.add(
                            ItemPage(
                                document.getTimestamp("deadline"),
                                document.getBoolean("deleted")!!,
                                document.getString("description"),
                                document.getString("ownerId"),
                                document.getString("password"),
                                document.getString("title"),
                                document.getString("unique_code")
                            )
                        )
                    }
                }
                callback.invoke(listPage)
            }
    }


}
