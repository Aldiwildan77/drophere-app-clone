package com.papbl.drophereclone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private val listPage = ArrayList<ItemPage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_page.layoutManager = LinearLayoutManager(activity)

        val item1 = ItemPage("5 Maret 2020 11:59", "Pengumpulan Laporan Praktikum Pemrograman Dasar", "abcd123")
        listPage.add(item1)
        val listPageAdapter = ManagePageAdapter(listPage)
        rv_page.adapter = listPageAdapter
//        val viewInflate = LayoutInflater.from(context).inflate(R.layout.text_input_unique, view, false)
        btn_kumpulkan_file.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.title_dialog_home))
                .setPositiveButton(resources.getString(R.string.submit)) { dialog, which ->

                }
                .setView(R.layout.text_input_unique)
                .show()
        }
    }



}
