package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.getScreenSize
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.str
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.container_color_sample_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*


class ColorSampleDialog(val activity: Activity, private val colorPack: ColorManager.ColorPack) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_color_sample_dlg, getScreenSize(context)[0] - dpToPx(80))
        setLayout()
    }

    private fun setLayout() {
        titleText.text = str(colorPack.titleId)
        titleIcon.visibility = View.GONE

        Glide.with(activity).load(colorPack.coverImgId).into(coverImg)
        val stickerImgs = arrayOf(
                colorImg0, colorImg1, colorImg2, colorImg3, colorImg4,
                colorImg5, colorImg6, colorImg7, colorImg8, colorImg9)
        stickerImgs.forEachIndexed { index, imageView ->
            imageView.setImageResource(R.drawable.normal_rect_fill)
            imageView.setColorFilter(colorPack.items[index])
        }
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
