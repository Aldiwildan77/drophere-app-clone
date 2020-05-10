package com.papbl.drophereclone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.papbl.drophereclone.models.ItemPage
import kotlinx.android.synthetic.main.component_card_pages.view.*
import java.text.SimpleDateFormat
import java.util.*


class ManagePageAdapter(private val listPage: ArrayList<ItemPage>) :
    RecyclerView.Adapter<ManagePageAdapter.ManageViewHolder>() {

    class ManageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(itemPage: ItemPage) {
            with(itemView) {
                if (itemPage.deadline == null) {
                    tv_deadline.text = context.getString(R.string.card_pages_tv_no_due_date)
                } else {
                    val simpleDateFormat =
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    simpleDateFormat.timeZone = TimeZone.getDefault()
                    val deadline = itemPage.deadline!!.toDate()
                    tv_deadline.text = simpleDateFormat.format(deadline)
                }
                tv_card_title.text = itemPage.title
                tv_unique_code_value.text = itemPage.unique_code
                card_page.setOnClickListener {
                    val intent = Intent(context, PengumpulanActivity::class.java).apply {
                        putExtra("unique_code", tv_unique_code_value?.text)
                    }
                    ManagePageActivity.homeFragment.startActivityForResult(intent, 200)
                }
                ib_unique_code_copy.setOnClickListener {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Kode Unik", tv_unique_code_value.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Kode unik berhasil disalin!", Toast.LENGTH_SHORT)
                        .show()
                }
                ib_more_popup.apply {
                    alpha = 0f
                    isEnabled = false
                }

            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ManageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_card_pages, parent, false)
        return ManageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listPage.size
    }

    override fun onBindViewHolder(holder: ManageViewHolder, position: Int) {
        holder.bind(listPage[position])
    }

}