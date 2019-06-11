package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.StickerManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_sticker_picker.view.*
import kotlinx.android.synthetic.main.list_item_sticker_picker_tab.view.*
import kotlinx.android.synthetic.main.pager_item_sticker_picker.view.*


class StickerPickerDialog(private val onResult: (StickerManager.Sticker) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_sticker_picker)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.isHideable = false
        root.viewPager.adapter = StickerPagerAdapter()
        root.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                root.recyclerView.scrollToPosition(position)
                root.recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        root.recyclerView.setBackgroundColor(AppTheme.backgroundColor)
        root.recyclerView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        root.recyclerView.adapter = TabAdapter()

        dialog.setOnShowListener { onShow() }
    }

    private fun onShow() {}

    inner class StickerPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_sticker_picker, null, false)
            val stickerImgs = arrayOf(
                    v.colorImg0, v.colorImg1, v.colorImg2, v.colorImg3,
                    v.colorImg4, v.colorImg5, v.colorImg6, v.colorImg7,
                    v.colorImg8, v.colorImg9, v.colorImg10, v.colorImg11,
                    v.colorImg12, v.colorImg13, v.colorImg14, v.colorImg15,
                    v.colorImg16, v.colorImg17, v.colorImg18, v.colorImg19)
            if(position == 0) {
                val recentPack = StickerManager.recentPack
                if(recentPack.isEmpty()) {
                    v.emptyLy.visibility = View.VISIBLE
                }else {
                    v.emptyLy.visibility = View.GONE
                    setStickerImage(stickerImgs, recentPack)
                }
            }else {
                val stickerPack = StickerManager.packs[position - 1]
                setStickerImage(stickerImgs, stickerPack.items.toList())
            }
            container.addView(v)
            return v
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
            (`object` as View).scrollView.isNestedScrollingEnabled = true
            for (i in 0 until count) {
                if (i != position) {
                    container.getChildAt(i).scrollView.isNestedScrollingEnabled = false
                }
            }
            container.requestLayout()
        }

        private fun setStickerImage(stickerImgs: Array<ImageView>, items: List<StickerManager.Sticker>) {
            stickerImgs.forEachIndexed { index, view ->
                if(index < items.size) {
                    val sticker = items[index]
                    view.visibility = View.VISIBLE
                    view.setImageResource(sticker.resId)
                    view.setOnClickListener {
                        StickerManager.updateRecentSticker(sticker)
                        onResult.invoke(sticker)
                        dismiss()
                    }
                }else {
                    view.visibility = View.GONE
                }
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = StickerManager.packs.size + 1
    }

    inner class TabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = StickerManager.packs.size + 1

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {}
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker_picker_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView

            if(position == 0) {
                val p = dpToPx(10)
                v.iconImg.setPadding(p,p,p,p)
                v.iconImg.setImageResource(R.drawable.recent)
            }else {
                val stickerPack = StickerManager.packs[position - 1]
                val p = dpToPx(5)
                v.iconImg.setPadding(p,p,p,p)
                v.iconImg.setImageResource(stickerPack.items[0].resId)
            }

            if(position == root.viewPager.currentItem) {
                v.iconImg.setBackgroundColor(AppTheme.backgroundDarkColor)
                if(position == 0) {
                    v.iconImg.setColorFilter(AppTheme.blueColor)
                    v.iconImg.alpha = 1f
                }else {
                    removeImageViewFilter(v.iconImg)
                }
            }else {
                v.iconImg.setBackgroundColor(Color.TRANSPARENT)
                if(position == 0) {
                    v.iconImg.setColorFilter(AppTheme.disableText)
                    v.iconImg.alpha = 1f
                }else {
                    setImageViewGrayFilter(v.iconImg)
                }
            }

            v.setOnClickListener {
                root.viewPager.currentItem = position
            }
        }
    }

}
