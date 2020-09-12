package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.container_edit_pack_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_sticker_pack_setting.view.*


class EditStickerPackDialog(val activity: BaseActivity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    private val selectedItems = StickerManager.packs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_edit_pack_dlg, getScreenSize(context)[0] - dpToPx(30))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.sticker_pack_setting)
        titleIcon.setImageResource(R.drawable.star)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = StickerManager.StickerPack.values().reversedArray()

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker_pack_setting, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val stickerPack = items[position]
            val v = holder.itemView

            v.titleText.text = str(stickerPack.titleId)
            v.imageView.setImageResource(stickerPack.items[0].resId)

            if(stickerPack.isPremium){
                v.premiumImg.visibility = View.VISIBLE
            }else {
                v.premiumImg.visibility = View.GONE
            }

            if(selectedItems.contains(stickerPack)) {
                v.selectedBox.visibility = View.VISIBLE
            }else {
                v.selectedBox.visibility = View.GONE
            }

            if(stickerPack.author != null) {
                v.authorText.visibility = View.VISIBLE
                v.authorText.text = "designed by ${stickerPack.author}"
            }else {
                v.authorText.visibility = View.GONE
            }

            v.setOnClickListener {
                if(selectedItems.contains(stickerPack)) {
                    selectedItems.remove(stickerPack)
                }else {
                    if(stickerPack.isPremium && !AppStatus.isPremium()){
                        showPremiumDialog(activity)
                    }else {
                        selectedItems.add(stickerPack)
                    }
                }
                notifyItemChanged(position)
            }

            v.setOnLongClickListener {
                showDialog(StickerSampleDialog(activity, stickerPack),
                        true, true, true, false)
                return@setOnLongClickListener true
            }
        }
    }

}
