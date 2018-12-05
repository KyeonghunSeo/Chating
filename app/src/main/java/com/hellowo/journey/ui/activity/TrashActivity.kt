package com.hellowo.journey.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellowo.journey.R
import com.hellowo.journey.ui.view.InkView
import kotlinx.android.synthetic.main.activity_draw.*

class TrashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        ink.setColor(getResources().getColor(android.R.color.black))
        ink.setMinStrokeWidth(1.5f)
        ink.setMaxStrokeWidth(6f)
        //ink.setFlags(InkView.FLAG_INTERPOLATION or InkView.FLAG_RESPONSIVE_WIDTH)
        ink.addListener(object : InkView.InkListener{
            override fun onInkClear() {

            }
            override fun onInkDraw() {
            }
        })
    }
}