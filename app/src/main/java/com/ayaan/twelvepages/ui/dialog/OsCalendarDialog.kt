package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.container_normal_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_normal.view.*
import java.util.HashSet


class OsCalendarDialog(activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    private val selectedItems = HashSet<String>(Prefs.getStringSet("osCalendarIds", HashSet<String>()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_normal_list_dlg, dpToPx(325))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.select_os_calendar)
        titleIcon.setImageResource(R.drawable.calendar)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()

        confirmBtn.setOnClickListener {
            Prefs.putStringSet("osCalendarIds", selectedItems)
            dismiss()
            onResult.invoke(true)
            MainActivity.getCalendarPager()?.redraw()
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items: List<OsCalendarManager.OsCalendar> = OsCalendarManager.getCalendarList(context)

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView

            v.titleText.text = item.title
            v.subText.text = item.accountName
            v.iconImg.setColorFilter(item.color)
            v.iconImg.setPadding(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3))

            if(selectedItems.contains(item.id.toString())) {
                v.checkImg.setImageResource(R.drawable.checked_fill)
            }else {
                v.checkImg.setImageResource(R.drawable.uncheck)
            }

            v.setOnClickListener {
                if(selectedItems.contains(item.id.toString())) {
                    selectedItems.remove(item.id.toString())
                }else {
                    selectedItems.add(item.id.toString())
                }
                notifyItemChanged(position)
            }
        }
    }

}
