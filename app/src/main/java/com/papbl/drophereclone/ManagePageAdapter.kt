package com.papbl.drophereclone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.papbl.drophereclone.models.ItemPage
import kotlinx.android.synthetic.main.component_card_pages.view.*
import java.util.*

class ManagePageAdapter(private val listPage: ArrayList<ItemPage>) :
    RecyclerView.Adapter<ManagePageAdapter.ManageViewHolder>() {

    class ManageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemPage: ItemPage) {
            with(itemView) {
                if (itemPage.deadline == null) {
                    tv_deadline.text = context.getString(R.string.card_pages_tv_no_due_date)
                } else {
                    val deadline = itemPage.deadline!!.toDate()
                    tv_deadline.text = deadline.toString()
                }
                tv_card_title.text = itemPage.title
                tv_unique_code_value.text = itemPage.unique_code
                ib_unique_code_copy.setOnClickListener {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Unique Code", tv_unique_code_value.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Unique Code Copied!", Toast.LENGTH_SHORT).show()
                }
                ib_more_popup.setOnClickListener {
                    PopupMenu(context, ib_more_popup).apply {
                        inflate(R.menu.option_menu_item)
                        setOnMenuItemClickListener { item ->

                            when (item!!.itemId) {
                                R.id.menu_item_pages_edit -> {
                                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                                }
                                R.id.menu_item_pages_delete -> {
                                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                                }
                            }
                            true
                        }
                        show()
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ManageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.component_card_pages, parent, false)
        return ManageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listPage.size
    }

    override fun onBindViewHolder(holder: ManageViewHolder, position: Int) {
        holder.bind(listPage[position])
    }

}