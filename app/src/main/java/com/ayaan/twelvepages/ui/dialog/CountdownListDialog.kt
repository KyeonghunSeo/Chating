package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.ui.activity.MainActivity
import kotlinx.android.synthetic.main.container_normal_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_normal.view.*


class CountdownListDialog(activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_normal_list_dlg, dpToPx(325))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.countdown)
        titleIcon.setImageResource(R.drawable.countdown)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = MainActivity.getViewModel()?.countdownRecords?.value

        override fun getItemCount(): Int = items?.size ?: 0

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            items?.get(position)?.let { record ->
                val v = holder.itemView
                v.titleText.text = record.getTitleInCalendar()

                v.setOnClickListener {
                    MainActivity.instance?.selectDate(record.dtStart)
                    dismiss()
                }
            }
        }
    }

}
