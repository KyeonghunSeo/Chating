package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.ayaan.twelvepages.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.activity_premium.backBtn
import kotlinx.android.synthetic.main.activity_premium.rootLy
import kotlinx.android.synthetic.main.pager_item_premium.view.*

class PremiumActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    private var bp: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        bp = BillingProcessor(this, str(R.string.in_app_license), this)
        initTheme(rootLy)
        initLayout()
        payBtn.setOnClickListener { subscribe() }
        callAfterViewDrawed(rootLy, Runnable{ leafFallView.start() })
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
                    v.titleText.text = str(R.string.premium_item_main_title)
                    v.subText.text = str(R.string.premium_item_main_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_system).into(v.imageView)
                }
                1 -> {
                    v.titleText.text = str(R.string.premium_item_0_title)
                    v.subText.text = str(R.string.premium_item_0_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_ad).into(v.imageView)
                }
                2 -> {
                    v.titleText.text = str(R.string.premium_item_1_title)
                    v.subText.text = str(R.string.premium_item_1_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_photo).into(v.imageView)
                }
                3 -> {
                    v.titleText.text = str(R.string.premium_item_2_title)
                    v.subText.text = str(R.string.premium_item_2_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_backup).into(v.imageView)
                }
                4 -> {
                    v.titleText.text = str(R.string.premium_item_3_title)
                    v.subText.text = str(R.string.premium_item_3_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_template).into(v.imageView)
                }
                else -> {
                    v.titleText.text = str(R.string.premium_item_4_title)
                    v.subText.text = str(R.string.premium_item_4_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.premium_ad).into(v.imageView)
                }
            }

            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = 6
    }

    override fun onBillingInitialized() {
        l("[onBillingInitialized]")
    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        //details?.purchaseInfo?.purchaseData?
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onStop() {
        super.onStop()
        leafFallView.stop()
    }

    public override fun onDestroy() {
        bp?.release()
        super.onDestroy()
    }
}