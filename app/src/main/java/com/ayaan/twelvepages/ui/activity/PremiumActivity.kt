package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import kotlinx.android.synthetic.main.activity_premium.*
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
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }

        versionText.text = "v ${packageManager.getPackageInfo(packageName, 0).versionName}"

        evaluateBtn.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.hellowo.day2life")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=com.hellowo.day2life")))
            }
        }

        emailBtn.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "bluelemonade@gmail.com", null))
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
            intent.data = Uri.parse("http://google.com")
            startActivity(intent)
        }
    }
}