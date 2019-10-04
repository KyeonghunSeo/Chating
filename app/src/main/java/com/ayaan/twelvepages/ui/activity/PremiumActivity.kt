package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.str
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.pager_item_premium.view.*

class PremiumActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    private var bp: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        //bp = BillingProcessor(this, AppConst.GOOGLE_IN_APP_LICENCE_KEY, this)
        initTheme(rootLy)
        initLayout()
        payBtn.setOnClickListener { subscribe() }
    }

    private fun subscribe() {
        bp?.subscribe(this, "premium")
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
            setGlobalTheme(v)

            when(position) {
                0 -> {
                    v.titleText.text = str(R.string.premium_item_0_title)
                }
                1 -> {
                    v.titleText.text = str(R.string.premium_item_1_title)
                }
                2 -> {
                    v.titleText.text = str(R.string.premium_item_2_title)
                }
                else -> {
                }
            }

            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = 4
    }

    override fun onBillingInitialized() {
    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }
}