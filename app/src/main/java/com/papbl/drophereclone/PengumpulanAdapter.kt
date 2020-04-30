package com.papbl.drophereclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PengumpulanAdapter(val items: ArrayList<SenderData>, val deadline: String) :
    RecyclerView.Adapter<PengumpulanHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengumpulanHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.component_card_submit, parent, false)
        return PengumpulanHolder(inflater)
    }

    override fun onBindViewHolder(holder: PengumpulanHolder, position: Int) {
        val senderData: SenderData = items[position]
        holder.bind(senderData, deadline)
    }
}

class PengumpulanHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener  {
    private var chipSubmitStatus = itemView.findViewById<Chip>(R.id.chip_submit_status)
    private var tvSendDate = itemView.findViewById<MaterialTextView>(R.id.tv_sender_date)
    private var tvSenderName = itemView.findViewById<MaterialTextView>(R.id.tv_sender_name)
    private var tvSenderFileName = itemView.findViewById<MaterialTextView>(R.id.tv_sender_file)

    init {
        v.setOnClickListener(this)
    }

    fun bind(senderData: SenderData, deadline: String) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        dateFormat.setTimeZone(TimeZone.getDefault())
        val deadlineDate = dateFormat.parse(deadline)
        val submitDate = senderData.submitAt.toDate()
        if (submitDate > deadlineDate) {
            chipSubmitStatus.text = itemView.context.getString(R.string.chips_submit_late)
            chipSubmitStatus.setChipBackgroundColorResource(R.color.colorError)
        } else {
            chipSubmitStatus.text = itemView.context.getString(R.string.chips_submit_on_time)
        }
        tvSendDate.text = dateFormat.format(submitDate)
        tvSenderName.text = senderData.fullName
        tvSenderFileName.text = senderData.fileName
    }

    override fun onClick(v: View) {
        Toast.makeText(v.context, tvSenderFileName.text, Toast.LENGTH_SHORT).show()
    }
}