package com.hellowo.journey.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hellowo.journey.R
import com.hellowo.journey.callAfterViewDrawed
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CustomListDialog
import kotlinx.android.synthetic.main.activity_about_us.*

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

    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }
        titleText.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("http://google.com")
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        leafFallView.stop()
    }
}