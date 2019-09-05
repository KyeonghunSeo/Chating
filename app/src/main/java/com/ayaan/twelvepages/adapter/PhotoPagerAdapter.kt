package com.ayaan.twelvepages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Link
import com.bumptech.glide.Glide
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.pager_item_photo.view.*

class PhotoPagerAdapter(private val context: Context, private val items: List<Link>, private val photoSize: Int,
                        private val cardMargin: Int) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = LayoutInflater.from(context).inflate(R.layout.pager_item_photo, null, false)
        v.photoCard.layoutParams = FrameLayout.LayoutParams(photoSize, (photoSize * 1.2).toInt()).apply {
            setMargins(cardMargin, 0, 0, 0)
        }
        v.photoView.layoutParams = FrameLayout.LayoutParams((photoSize * 1.2).toInt(), (photoSize * 1.2).toInt())
        Glide.with(context)
                .load(items[position].strParam0)
                .into(v.photoView)
        v.photoCard.setOnClickListener {
            ImageViewer.Builder(context, items.map { it.strParam0 })
                    .hideStatusBar(false)
                    .setStartPosition(position)
                    .show()
        }
        container.addView(v)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
    override fun getCount(): Int = items.size
}