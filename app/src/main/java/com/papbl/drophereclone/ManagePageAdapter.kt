package com.papbl.drophereclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_page.view.*

class ManagePageAdapter(private val listPage: ArrayList<ItemPage>) : RecyclerView.Adapter<ManagePageAdapter.ManageViewHolder>(){
    class ManageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemPage: ItemPage) {
            with(itemView){
                tv_deadline.text = itemPage.deadline
                tv_card_title.text = itemPage.title
                tv_unique_code_value.text = itemPage.uniqueCode
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ManagePageAdapter.ManageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return ManageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listPage.size
    }

    override fun onBindViewHolder(holder: ManagePageAdapter.ManageViewHolder, position: Int) {
        holder.bind(listPage[position])
    }

}