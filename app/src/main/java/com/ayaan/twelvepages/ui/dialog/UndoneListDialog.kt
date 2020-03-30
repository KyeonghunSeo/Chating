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
import kotlinx.android.synthetic.main.list_item_simple_record.view.*
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
                container.checkBtn.visibility = View.VISIBLE
                container.countdownText.visibility = View.GONE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_simple_record, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            items?.get(position)?.let { record ->
                val v = holder.itemView
                v.checkBox.setColorFilter(record.getColor())
                v.titleText.text = record.getTitleInCalendar()
                v.memoText.text = "${AppDateFormat.ymde.format(Date(record.dtEnd))} (${record.getDueText(System.currentTimeMillis())})"
                v.checkBtn.setOnClickListener {
                    val pos = items.indexOf(record)
                    RecordManager.done(record)
                    if(items.size == 0) {
                        dismiss()
                        toast(R.string.all_doned, R.drawable.all_done)
                    }else{
                        notifyItemRemoved(pos)
                    }
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
