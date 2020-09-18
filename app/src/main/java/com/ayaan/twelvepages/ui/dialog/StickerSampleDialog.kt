package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.getScreenSize
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.str
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.container_custom_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*


class StickerSampleDialog(val activity: Activity, private val stickerPack: StickerManager.StickerPack) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_custom_list_dlg, getScreenSize(context)[0] - dpToPx(80))
        setLayout()
    }

    private fun setLayout() {
        recyclerView.layoutManager = GridLayoutManager(context, 5)
        recyclerView.adapter = StickerAdapter(stickerPack.items)
        recyclerView.setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(20))
        titleText.setPadding(dpToPx(15), 0,0, 0)
        titleText.text = str(stickerPack.titleId)
        titleIcon.visibility = View.GONE
        subText.visibility = View.GONE
        hideBottomBtnsLy()
    }

    inner class StickerAdapter(val items: Array<StickerManager.Sticker>) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sticker = items[position]
            val v = holder.itemView
            Glide.with(activity).load(sticker.resId).into(v.findViewById(R.id.imageView))
        }
    }

}
