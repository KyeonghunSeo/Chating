package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import kotlinx.android.synthetic.main.container_normal_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_undone.view.*
import java.util.*


class UndoneListDialog(activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_normal_list_dlg, getScreenSize(context)[0] - dpToPx(50))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.undone_records)
        titleIcon.setImageResource(R.drawable.check)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.text = str(R.string.no_show_this_day)
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.visibility = View.GONE
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = MainActivity.getViewModel()?.undoneRecords?.value

        override fun getItemCount(): Int = items?.size ?: 0

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_undone, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            items?.get(position)?.let { record ->
                val v = holder.itemView
                v.colorBar.setCardBackgroundColor(record.getColor())
                v.titleText.text = record.getTitleInCalendar()
                v.subText.text = "${AppDateFormat.ymde.format(Date(record.dtEnd))} (${record.getDueText(System.currentTimeMillis())})"

                v.checkBtn.setOnClickListener {
                    if(items.size == 1) {
                        dismiss()
                    }
                    RecordManager.done(record)
                    notifyItemRemoved(position)
                }

                v.setOnClickListener {
                    MainActivity.instance?.selectDate(record.dtEnd)
                    toast(R.string.moved, R.drawable.schedule)
                    dismiss()
                }
            }
        }
    }

}
