package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

        aboutUsText.text = when(Locale.getDefault().language) {
            "ko" -> {
                """이제는 찾을수 없는
                    마음을 울렸던 책의 한 구절을 적어두었던
포스트잇은 어디로 사라진걸까?

문득 기록하지 않아서 잊혀지는 것들이
너무나 많다는 생각이 들었습니다.

계속해서 발전해 나갈것입니다.

사용해 주셔서 감사합니다."""
            }
            else -> {
                """

                """
            }
        }
    }

    override fun onStop() {
        super.onStop()
        leafFallView.stop()
    }
}