package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.ayaan.twelvepages.*
import kotlinx.android.synthetic.main.container_alarm_picker.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*

class AlarmPickerDialog(private val activity: Activity, private var offset: Long, private var dtAlarm: Long,
                        private val onResult: (Boolean, Long, Long) -> Unit) : BaseDialog(activity) {
    private val offsets = arrayOf(
            0,
            1000L * 60 * 60 * 9,
            1000L * 60 * 60 * 12,
            1000L * 60 * 60 * 18,
            -1000L * 60 * 30,
            -1000L * 60 * 60,
            -1000L * 60 * 60 * 24,
            -1000L * 60 * 60 * 24 * 7)

    private var isTemplate = false

    init {
        if(dtAlarm == Long.MIN_VALUE) {
            dtAlarm = getTommorow9oclock()
        }else if(dtAlarm == Long.MAX_VALUE) {
            isTemplate = true
            dtAlarm = getTommorow9oclock()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_alarm_picker, dpToPx(325))
        titleText.text = context.getString(R.string.set_alarm)
        titleIcon.setImageResource(R.drawable.alarm)
        setGlobalTheme(rootLy)

        confirmBtn.setOnClickListener {
            onResult.invoke(true, offset, dtAlarm)
            dismiss()
        }

        cancelBtn.text = context.getString(R.string.delete_alarm)
        cancelBtn.setOnClickListener {
            onResult.invoke(false, offset, dtAlarm)
            dismiss()
        }

        val btns = arrayOf(alarmBtn0, alarmBtn1, alarmBtn2, alarmBtn3,
                alarmBtn4, alarmBtn5, alarmBtn6, alarmBtn7)

        btns.forEachIndexed { index, view ->
            view.setOnClickListener {
                offset = offsets[index]
                setBtns(btns)
            }
        }

        customTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(activity, dtAlarm) { time ->
                offset = Long.MIN_VALUE
                dtAlarm = time
                setBtns(btns)
            }, true, true, true, false)
        }

        setBtns(btns)
    }

    @SuppressLint("SetTextI18n")
    private fun setBtns(btns: Array<TextView>) {
        inActiveBtn(customTimeBtn)
        btns.forEachIndexed { index, view ->
            inActiveBtn(view)
        }

        val index = offsets.indexOf(offset)
        if(index >= 0) {
            activeBtn(btns[index])
            customTimeBtn.text = context.getString(R.string.custom_time)
        }else {
            activeBtn(customTimeBtn)
            if(isTemplate) {
                customTimeBtn.text = AppDateFormat.time.format(Date(dtAlarm))
            }else {
                customTimeBtn.text = AppDateFormat.dateTime.format(Date(dtAlarm))
            }
        }
    }

    private fun activeBtn(view: TextView) {
        view.setTextColor(AppTheme.backgroundColor)
        view.typeface = AppTheme.boldFont
        view.setBackgroundColor(AppTheme.primaryText)
        view.alpha = 1f
    }

    private fun inActiveBtn(view: TextView) {
        view.setTextColor(AppTheme.primaryColor)
        view.typeface = AppTheme.regularFont
        view.setBackgroundColor(AppTheme.disableText)
        view.alpha = 0.4f
    }
}
