package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.model.Link
import kotlinx.android.synthetic.main.container_countdown.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*

class CountdownDialog(private val activity: Activity, val link: Link?,
                      private val onResult: (Boolean, Link) -> Unit) : BaseDialog(activity) {
    val countdown = Link(type = Link.Type.COUNTDOWN.ordinal)

    init {
        link?.let {
            countdown.intParam0 = it.intParam0
            countdown.intParam1 = it.intParam1
            countdown.intParam2 = it.intParam2
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_countdown, dpToPx(325))
        titleText.text = context.getString(R.string.countdown)
        titleIcon.setImageResource(R.drawable.countdown)

        confirmBtn.setOnClickListener {
            onResult.invoke(true, countdown)
            dismiss()
        }

        cancelBtn.text = context.getString(R.string.delete_countdown)
        cancelBtn.setTextColor(AppTheme.red)
        cancelBtn.setOnClickListener {
            onResult.invoke(false, countdown)
            dismiss()
        }

        countdownTypeBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.dday), subText = str(R.string.dday_sub)),
                            PopupOptionDialog.Item(str(R.string.anniversary), subText = str(R.string.anniversary_sub))),
                    countdownTypeBtn, true) { index ->
                countdown.intParam0 = index
                setViews()
            }, true, false, true, false)
        }

        countdownFunBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.count)),
                            PopupOptionDialog.Item(str(R.string.no_count))),
                    countdownFunBtn, true) { index ->
                countdown.intParam1 = index
                setViews()
            }, true, false, true, false)

        }

        setViews()
    }

    private fun setViews() {
        countdownTypeText.text = when(countdown.intParam0) {
            0 -> str(R.string.dday)
            else -> str(R.string.anniversary)
        }

        countdownFunText.text = when(countdown.intParam1) {
            0 -> str(R.string.count)
            else -> str(R.string.no_count)
        }
    }
}
