package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_popup_option.*
import kotlinx.android.synthetic.main.pager_item_color_palette.view.*


class StickerPickerDialog(activity: Activity, private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val pagerSize = dpToPx(300)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_popup_option)
        rootLy.setOnClickListener { dismiss() }
        setGlobalTheme(rootLy)

    }


    inner class ColorPalettePagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_color_palette, null, false)
            val palette = AppTheme.ColorPalette.values()[position]
            v.imageView.setImageResource(palette.coverImgId)
            v.titleText.text = context.getString(palette.titleId)
            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = AppTheme.ColorPalette.values().size
    }

}
