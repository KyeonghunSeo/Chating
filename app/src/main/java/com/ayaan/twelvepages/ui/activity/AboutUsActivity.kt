package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import kotlinx.android.synthetic.main.activity_about_us.*
import java.util.*

class AboutUsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        initTheme(rootLy)
        initLayout()
        callAfterViewDrawed(rootLy, Runnable{ leafFallView.start() })
    }

    @SuppressLint("SetTextI18n")
    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, y: Int, _: Int, _: Int ->
            if(y > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }

        evaluateBtn.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.ayaan.twelvepages")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.ayaan.twelvepages")))
            }
        }

        emailBtn.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "thetwelvepages@gmail.com", null))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "[${getString(R.string.help)}]")
            sendIntent.putExtra(Intent.EXTRA_TEXT, "\n\n[Device information]\n" +
                    "Language : ${Locale.getDefault().language}\n" +
                    "OS version : ${Build.VERSION.RELEASE}\n" +
                    "Brand : ${Build.BRAND}\n" +
                    "Device : ${Build.MODEL}\n" +
                    "App version : ${packageManager.getPackageInfo(packageName, 0).versionName}")
            startActivity(sendIntent)
        }

        voteBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSeiXv4Ng9lrgD44oCNXHOYuv5FC1dJwZ3ZRKYXnmLO3aFSA3g/viewform?usp=sf_link")
            startActivity(intent)
        }

        aboutUsText.typeface = ResourcesCompat.getFont(this, R.font.regular_s)
        aboutUsText.text = when(Locale.getDefault().language) {
            "ko" -> {
                """이제는 기억나지 않는
마음을 울렸던
어느 책의 한 구절 처럼

기록하지 않아 사라진 것들에 대한
아쉬움을 담아

흘러가는 일상을 적는
다이어리를 만듭니다.
                    """
            }
            else -> {
                """With regret
about the things that
have disappeared without recording

Make a diary
to write down the daily life.
                """
            }
        }
    }

    override fun onStop() {
        super.onStop()
        leafFallView.stop()
    }
}