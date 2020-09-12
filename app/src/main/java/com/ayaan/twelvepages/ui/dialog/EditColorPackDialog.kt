package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.container_edit_pack_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_color_pack_setting.view.*
import kotlinx.android.synthetic.main.list_item_color_pack_setting.view.selectedBox
import kotlinx.android.synthetic.main.list_item_color_pack_setting.view.titleText


class EditColorPackDialog(val activity: Activity, val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    private val selectedItems = ColorManager.packs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_edit_pack_dlg, getScreenSize(context)[0] - dpToPx(30))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.color_pack_setting)
        titleIcon.setImageResource(R.drawable.color)
        subText.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ItemAdapter()
        confirmBtn.setOnClickListener {
            dismiss()
            onResult.invoke(true)
        }
        cancelBtn.setOnClickListener { dismiss() }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = ColorManager.ColorPack.values().reversedArray()

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
            Glide.with(activity).load(colorPack.coverImgId).into(v.coverImg)
            v.sample0.setColorFilter(colorPack.items[0])
            v.sample1.setColorFilter(colorPack.items[1])
            v.sample2.setColorFilter(colorPack.items[2])
            v.sample3.setColorFilter(colorPack.items[3])
            v.sample4.setColorFilter(colorPack.items[4])
            v.sample5.setColorFilter(colorPack.items[5])
            v.sample6.setColorFilter(colorPack.items[6])
            v.sample7.setColorFilter(colorPack.items[7])
            v.sample8.setColorFilter(colorPack.items[8])
            v.sample9.setColorFilter(colorPack.items[9])

            if(colorPack.isPremium){
                v.premiumImg.visibility = View.VISIBLE
            }else {
                v.premiumImg.visibility = View.GONE
            }

            if(selectedItems.contains(colorPack)) {
                v.selectedBox.visibility = View.VISIBLE
            }else {
                v.selectedBox.visibility = View.GONE
            }

            v.setOnClickListener {
                if(selectedItems.contains(colorPack)) {
                    selectedItems.remove(colorPack)
                }else {
                    selectedItems.add(colorPack)
                }
                notifyItemChanged(position)
            }

            v.setOnLongClickListener {
                showDialog(ColorSampleDialog(activity, colorPack),
                        true, true, true, false)
                return@setOnLongClickListener true
            }
        }
    }

}
