package com.hellowo.journey.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.hellowo.journey.*
import com.hellowo.journey.ui.dialog.CustomListDialog
import kotlinx.android.synthetic.main.activity_about_us.*
import java.util.*

class AboutUsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        initTheme(rootLy)
        initLayout()
        callAfterViewDrawed(rootLy, Runnable{
            leafFallView.start()
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }

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

        aboutUsText.text = when(Locale.getDefault().language) {
            "ko" -> {
                """마음을 울렸던 책의 한 구절을 적어두었던
포스트잇은 어디로 사라진걸까?

언젠가 꼭 보기로 마음먹었던
영화 제목은 왜 항상 기억이 안나는걸까?

문득 기록하지 않아서 잊혀지는 것들이
너무나 많다는 생각이 들었습니다.

'기록의 계절'은 이 물음들에

바쁜 일정들과
산더미처럼 쌓인 업무
친구와의 일상의 대화
스쳐지나가는 상념
소박한 바램들부터
원대한 꿈까지

모든 것들을 한곳에 기록하고, 정리한다면

분명 삶은 조금 더 명료해지고
다가올 미래를 더 잘 계획할 수 있을겁니다.

그리고 어쩌면 과거의 기록들을 통하여
생각지도 못했던 영감을 얻고
새로운 열정을 불태울지도 모르죠.

'기록의 계절'은 기록의 힘을 믿고
기록을 통하여 성장할 수 있도록 도와주는
좋은 도구가 될 수 있도록
계속해서 발전해 나가겠습니다.



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