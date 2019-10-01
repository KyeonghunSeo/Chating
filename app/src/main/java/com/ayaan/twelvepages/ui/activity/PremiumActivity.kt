package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.PagerAdapter
import com.ayaan.twelvepages.*
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.pager_item_premium.*
import java.util.*

class PremiumActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        initTheme(rootLy)
        initLayout()
    }

    @SuppressLint("SetTextI18n")
    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }
        viewPager.adapter = Adapter()
        viewPager.offscreenPageLimit = 2
        viewPager.pageMargin = -dpToPx(80)
    }

    inner class Adapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(this@PremiumActivity).inflate(R.layout.pager_item_premium, null, false)
            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = 4
    }
}