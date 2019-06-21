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
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.StickerManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.android.synthetic.main.list_item_color_picker_tab.view.*
import kotlinx.android.synthetic.main.pager_item_color_picker.view.*


class ColorPickerDialog(private val onResult: (Int) -> Unit) : BottomSheetDialog() {
    var isCoverOpened = false

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_color_picker)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.isHideable = false
        val pagerAdapter = ColorPagerAdapter()
        root.viewPager.adapter = pagerAdapter
        root.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
                if(!isCoverOpened) {
                    isCoverOpened = true
                    TransitionManager.beginDelayedTransition(root.viewPagerContainer, makeChangeBounceTransition())
                    for (i in 0 until pagerAdapter.count) {
                        pagerAdapter.container?.getChildAt(i)?.coverImg?.visibility = View.VISIBLE
                    }
                    root.viewPagerContainer.layoutParams.height += dpToPx(250)
                    root.viewPagerContainer.requestLayout()
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                root.recyclerView.scrollToPosition(position)
                root.recyclerView.adapter?.notifyDataSetChanged()
            }
        })
        root.recyclerView.setBackgroundColor(AppTheme.background)
        root.recyclerView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        root.recyclerView.adapter = TabAdapter()
        dialog.setOnShowListener { onShow() }
    }

    private fun onShow() {}

    inner class ColorPagerAdapter : PagerAdapter() {
        var container: ViewGroup? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            this.container = container
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_color_picker, null, false)
            val stickerImgs = arrayOf(
                    v.colorImg0, v.colorImg1, v.colorImg2, v.colorImg3, v.colorImg4,
                    v.colorImg5, v.colorImg6, v.colorImg7, v.colorImg8, v.colorImg9)
            if(position == 0) {
                Glide.with(context!!).load(R.drawable.color_palette_0).into(v.coverImg)
                val recentPack = ColorManager.recentPack
                if(recentPack.isEmpty()) {
                    v.emptyLy.visibility = View.VISIBLE
                }else {
                    v.emptyLy.visibility = View.GONE
                    stickerImgs.forEachIndexed { index, imageView ->
                        val colorKey = if(index < recentPack.size) {
                            recentPack[index]
                        }else {
                            9
                        }
                        imageView.setColorFilter(ColorManager.getColor(colorKey))
                        imageView.setOnClickListener {
                            ColorManager.updateRecentColor(colorKey)
                            onResult.invoke(colorKey)
                            dismiss()
                        }
                    }
                }
            }else {
                val colorPack = ColorManager.packs[position - 1]
                Glide.with(context!!).load(colorPack.coverImgId).into(v.coverImg)
                stickerImgs.forEachIndexed { index, imageView ->
                    val colorKey = colorPack.ordinal * ColorManager.colorPaletteSize + index
                    imageView.setColorFilter(colorPack.items[index])
                    imageView.setOnClickListener {
                        ColorManager.updateRecentColor(colorKey)
                        onResult.invoke(colorKey)
                        dismiss()
                    }
                }
            }
            container.addView(v)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = ColorManager.packs.size + 1
    }

    inner class TabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = ColorManager.packs.size + 1

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {}
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView

            if(position == 0) {
                v.iconImg.visibility = View.VISIBLE
                v.colorSampleView.visibility = View.GONE
                v.iconImg.setImageResource(R.drawable.recent)
            }else {
                v.iconImg.visibility = View.GONE
                v.colorSampleView.visibility = View.VISIBLE
                val colorPack = ColorManager.packs[position - 1]
                v.sample0.setColorFilter(colorPack.items[0])
                v.sample1.setColorFilter(colorPack.items[2])
                v.sample2.setColorFilter(colorPack.items[4])
                v.sample3.setColorFilter(colorPack.items[8])
            }

            if(position == root.viewPager.currentItem) {
                if(position == 0) {
                    v.iconImg.setBackgroundColor(AppTheme.backgroundDark)
                    v.iconImg.setColorFilter(AppTheme.primary)
                    v.iconImg.alpha = 1f
                }else {
                    v.colorSampleView.setBackgroundColor(AppTheme.backgroundDark)
                    v.colorSampleView.alpha = 1f
                }
            }else {
                if(position == 0) {
                    v.iconImg.setBackgroundColor(Color.TRANSPARENT)
                    v.iconImg.setColorFilter(AppTheme.disableText)
                    v.iconImg.alpha = 1f
                }else {
                    v.colorSampleView.setBackgroundColor(Color.TRANSPARENT)
                    v.colorSampleView.alpha = 0.5f
                }
            }

            v.setOnClickListener {
                root.viewPager.currentItem = position
            }
        }
    }

}
