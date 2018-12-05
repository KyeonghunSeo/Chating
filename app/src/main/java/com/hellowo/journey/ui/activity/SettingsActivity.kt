package com.hellowo.journey.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellowo.journey.R
import com.hellowo.journey.ui.view.InkView
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initTheme(rootLy)
    }
}