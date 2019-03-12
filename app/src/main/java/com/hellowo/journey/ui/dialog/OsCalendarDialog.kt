package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.dialog_normal_list.*
import kotlinx.android.synthetic.main.list_item_normal.view.*
import java.util.HashSet


class OsCalendarDialog(activity: Activity) : Dialog(activity) {
    private val selectedItems = HashSet<String>(Prefs.getStringSet("osCalendarIds", HashSet<String>()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_normal_list)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        titleText.text = context.getString(R.string.select_os_calendar)
        subText.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()

        confirmBtn.setOnClickListener { _ ->
            Prefs.putStringSet("osCalendarIds", selectedItems)
            dismiss()
            MainActivity.getCalendarPagerView()?.redraw()
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

            if(selectedItems.contains(item.id.toString())) {
                v.iconImg.setImageResource(R.drawable.sharp_check_box_black_48dp)
            }else {
                v.iconImg.setImageResource(R.drawable.sharp_check_box_outline_blank_black_48dp)
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
