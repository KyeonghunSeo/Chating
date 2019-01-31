package com.hellowo.journey.ui.activity

import android.os.Bundle
import com.hellowo.journey.R
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CustomListDialog
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initTheme(rootLy)
        initLayout()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }

        startdowBtn.setOnClickListener {
            showDialog(CustomListDialog(this@SettingsActivity,
                    getString(R.string.startdow),
                    getString(R.string.startdow_sub),
                    null,
                    false,
                    resources.getStringArray(R.array.day_of_weeks).toList()) {

            }, true, true, true, false)
        }
    }
}