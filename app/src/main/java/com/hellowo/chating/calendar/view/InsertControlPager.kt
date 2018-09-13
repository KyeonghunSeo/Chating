package com.hellowo.chating.calendar.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.chating.R

class InsertControlPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    val items = listOf("", "", "", "")

    init {
        adapter = InsertPageAdapter()
        addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            private var jumpPosition = -1

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE && jumpPosition >= 0) {
                    //Jump without animation so the user is not aware what happened.
                    setCurrentItem(jumpPosition, false)
                    //Reset jump position.
                    jumpPosition = -1
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> //prepare to jump to the last page
                        jumpPosition = getRealCount()
                    getRealCount() + 1 -> //prepare to jump to the first page
                        jumpPosition = 1
                    else -> {

                    }
                }
            }
        })
        setCurrentItem(1, false)
    }

    private fun getRealCount(): Int {
        return items.size + 1
    }

    private fun mapPagerPositionToModelPosition(pagerPosition: Int): Int {
        // Put last page model to the first position.
        if (pagerPosition == 0) {
            return getRealCount() - 1
        }
        // Put first page model to the last position.
        return if (pagerPosition == getRealCount() + 1) {
            0
        } else pagerPosition - 1
    }

    private inner class InsertPageAdapter : PagerAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int = items.size + 3

        @SuppressLint("InflateParams")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = mInflater.inflate(R.layout.item_insert_control_pager, null)
            (container as ViewPager).addView(v, 0)
            return v
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View?)
        }
        override fun restoreState(arg0: Parcelable?, arg1: ClassLoader?) {}
        override fun saveState(): Parcelable? = null
    }
}