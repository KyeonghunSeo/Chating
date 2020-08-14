package com.ayaan.twelvepages.ui.activity

import android.os.Bundle
import com.ayaan.twelvepages.R
import kotlinx.android.synthetic.main.activity_draw.*

class TrashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        initTheme(rootLy)
    }
}