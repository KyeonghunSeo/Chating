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
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.str
import kotlinx.android.synthetic.main.container_normal_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_sticker_pack_setting.view.*


class EditStickerPackDialog(activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    private val selectedItems = StickerManager.packs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_normal_list_dlg, dpToPx(325))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.sticker_pack_setting)
        titleIcon.setImageResource(R.drawable.star)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = StickerManager.StickerPack.values()

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker_pack_setting, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val stickerPack = items[position]
            val v = holder.itemView

            v.titleText.text = str(stickerPack.titleId)
            v.imageView.setImageResource(stickerPack.items[0].resId)

            if(selectedItems.contains(stickerPack)) {
                v.checkImg.setImageResource(R.drawable.checked_fill)
            }else {
                v.checkImg.setImageResource(R.drawable.uncheck)
            }

            v.setOnClickListener {
                if(selectedItems.contains(stickerPack)) {
                    selectedItems.remove(stickerPack)
                }else {
                    selectedItems.add(stickerPack)
                }
                notifyItemChanged(position)
            }
        }
    }

}
