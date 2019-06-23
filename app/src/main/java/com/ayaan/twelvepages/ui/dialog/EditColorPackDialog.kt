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
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.str
import kotlinx.android.synthetic.main.container_normal_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_color_pack_setting.view.*


class EditColorPackDialog(activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    private val selectedItems = ColorManager.packs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_normal_list_dlg, dpToPx(325))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.color_pack_setting)
        titleIcon.setImageResource(R.drawable.color)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = ColorManager.ColorPack.values()

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_pack_setting, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val colorPack = items[position]
            val v = holder.itemView

            v.titleText.text = str(colorPack.titleId)
            v.sample0.setColorFilter(colorPack.items[0])
            v.sample1.setColorFilter(colorPack.items[2])
            v.sample2.setColorFilter(colorPack.items[4])
            v.sample3.setColorFilter(colorPack.items[6])

            if(selectedItems.contains(colorPack)) {
                v.checkImg.setImageResource(R.drawable.checked_fill)
            }else {
                v.checkImg.setImageResource(R.drawable.uncheck)
            }

            v.setOnClickListener {
                if(selectedItems.contains(colorPack)) {
                    selectedItems.remove(colorPack)
                }else {
                    selectedItems.add(colorPack)
                }
                notifyItemChanged(position)
            }
        }
    }

}
