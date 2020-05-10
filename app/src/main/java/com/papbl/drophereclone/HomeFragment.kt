package com.papbl.drophereclone

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.papbl.drophereclone.models.ItemPage
import com.papbl.drophereclone.utils.UserCredential
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private val listPage: ArrayList<ItemPage> = arrayListOf()
    private val db = FirebaseFirestore.getInstance()
    private val pageCollection = db.collection("pages")
    private val credential = UserCredential()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showRecyclerList()
        btn_home_submit_file.setOnClickListener {
            val dialogView: View = layoutInflater.inflate(R.layout.input_unique_code, null)
            val textUniqueCode: TextInputEditText = dialogView.findViewById(R.id.tv_search_unique)
            MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.dialog_submit_unique_code_title))
                .setView(dialogView)
                .setPositiveButton(resources.getString(R.string.dialog_submit)) { _, _ ->
                    val uniqueCode = textUniqueCode.text.toString()
                    findPage(uniqueCode)
                }.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listPage.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            listPage.clear()
            doPageListing()
        }
    }

    private fun findPage(uniqueCode: String) {
        pageCollection
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.getString("unique_code") == uniqueCode) {
                        val page = ItemPage(
                            document.id,
                            document.getTimestamp("deadline"),
                            document.getBoolean("deleted")!!,
                            document.getString("description"),
                            document.getString("ownerId"),
                            document.getString("password"),
                            document.getString("title"),
                            document.getString("unique_code")
                        )
                        if (document.getString("password") != null) {
                            inputPasswordCheckDialog(page)
                            return@addOnSuccessListener
                        } else {
                            val intentTest = Intent(activity, SubmissionActivity::class.java)
                            intentTest.putExtra("extra_page", page)
                            startActivity(intentTest)
                            return@addOnSuccessListener
                        }
                    }
                }
                Toast.makeText(
                    context,
                    resources.getString(R.string.error_dialog_submit_unavailable_unique_code),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    @SuppressLint("InflateParams")
    private fun inputPasswordCheckDialog(page: ItemPage) {
        val dialogView: View = layoutInflater.inflate(R.layout.input_password, null)
        val textPassword: TextInputEditText = dialogView.findViewById(R.id.tv_password_check)
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.dialog_submit_password_title))
            .setPositiveButton(resources.getString(R.string.dialog_submit)) { _, _ ->
                val textInputPassword = textPassword.text.toString()
                if (textInputPassword == page.password) {
                    val intent = Intent(activity, SubmissionActivity::class.java)
                    intent.putExtra("extra_page", page)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.error_dialog_submit_password_is_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setView(dialogView)
            .show()
    }

    private fun showRecyclerList() {
        doPageListing()
    }

    private fun doPageListing() {
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
        pageCollection
            .get()
            .addOnSuccessListener { result ->
                val listPage: ArrayList<ItemPage> = arrayListOf()
                for (document in result) {
                    if (document.getString("ownerId") == credential.getLoggedUser(requireActivity()).uid
                        && !(document.getBoolean("deleted") as Boolean)
                    ) {
                        listPage.add(
                            ItemPage(
                                document.id,
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
