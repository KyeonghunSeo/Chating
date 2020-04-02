package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.bumptech.glide.Glide
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.activity_premium.backBtn
import kotlinx.android.synthetic.main.activity_premium.rootLy
import kotlinx.android.synthetic.main.pager_item_premium.view.*
import java.util.*

class PremiumActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    private var bp: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        bp = BillingProcessor(this, str(R.string.in_app_license), this)
        initTheme(rootLy)
        initLayout()
        callAfterViewDrawed(rootLy, Runnable{ leafFallView.start() })
    }

    @SuppressLint("SetTextI18n")
    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }
        viewPager.adapter = Adapter()
        viewPager.offscreenPageLimit = 2
        viewPager.pageMargin = -dpToPx(80)
        unSubscribeText.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://support.google.com/googleplay/answer/7018481?co=GENIE.Platform%3DAndroid&hl=" + Locale.getDefault().language)
            startActivity(intent)
        }
        setPayLy()
    }

    inner class Adapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(this@PremiumActivity).inflate(R.layout.pager_item_premium, null, false)
            setGlobalTheme(v)

            when(position) {
                0 -> {
                    v.titleText.text = str(R.string.premium_item_1_title)
                    v.subText.text = str(R.string.premium_item_1_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.photo_sample).into(v.imageView)
                }
                1 -> {
                    v.titleText.text = str(R.string.premium_item_4_title)
                    v.subText.text = str(R.string.premium_item_4_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.sticker_sample).into(v.imageView)
                }
                2 -> {
                    v.titleText.text = str(R.string.premium_item_3_title)
                    v.subText.text = str(R.string.premium_item_3_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.template_sample).into(v.imageView)
                }
                3 -> {
                    v.titleText.text = str(R.string.premium_item_0_title)
                    v.subText.text = str(R.string.premium_item_0_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.ad_sample).into(v.imageView)
                }
                else -> {
                    v.titleText.text = str(R.string.premium_item_2_title)
                    v.subText.text = str(R.string.premium_item_2_sub)
                    Glide.with(this@PremiumActivity).load(R.drawable.backup_sample).into(v.imageView)
                }
            }

            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = 5
    }

    private fun setPayLy() {
        if(AppStatus.isPremium()) {
            payLy.alpha = 0.3f
            payText.text = str(R.string.you_are_premium)
            payBtn.setOnClickListener {}
        }else {
            payLy.alpha = 1f
            payText.text = str(R.string.upgrade_premium)
            payBtn.setOnClickListener { subscribe() }
        }
    }

    override fun onBillingInitialized() {}
    override fun onPurchaseHistoryRestored() {}
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        //details?.purchaseInfo?.purchaseData?
        if(productId == "premium") {
            purchased()
        }
    }
    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    private fun subscribe() {
        bp?.subscribe(this, "premium")
    }

    private fun purchased() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        AppStatus.premiumTime = cal.timeInMillis
        Prefs.putLong("premiumTime", AppStatus.premiumTime)
        setPayLy()
        MainActivity.instance?.setPremium()
        val dialog = CustomDialog(this, getString(R.string.thank_you_subscribe),
                getString(R.string.thank_you_subscribe_sub), null, R.drawable.crown) { result, _, _ ->
        }
        showDialog(dialog, true, true, true, false)
        dialog.hideBottomBtnsLy()
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