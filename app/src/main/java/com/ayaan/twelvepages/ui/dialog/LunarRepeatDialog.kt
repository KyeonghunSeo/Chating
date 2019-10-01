package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.KoreanLunarCalendar
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.container_lunar_repeat_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import org.json.JSONObject
import java.util.*


class LunarRepeatDialog(activity: Activity, record: Record,
                        private val onResult: (String?, Long) -> Unit) : BaseDialog(activity) {
    private val cal = Calendar.getInstance()
    private val lunarCal = KoreanLunarCalendar.getInstance()

    init {
        cal.timeInMillis = record.dtStart
        lunarCal.setSolarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_lunar_repeat_dlg, dpToPx(325))
        titleText.text = context.getString(R.string.set_alarm)
        titleIcon.setImageResource(R.drawable.lunar)
        setLayout()
    }

    private fun setLayout() {
        titleText.text = lunarCal.lunarFormat

        datePicker.date = cal.timeInMillis
        datePicker.setOnDateChangeListener { _, y, m, d ->
            lunarCal.setSolarDate(y, m+1, d)
            titleText.text = lunarCal.lunarFormat
        }

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            val jsonObject = JSONObject()
            jsonObject.put("freq", 4)
            jsonObject.put("interval", 1)
            jsonObject.put("weekNum", "0000000")
            jsonObject.put("monthOption", 0)
            jsonObject.put("lunar", true)
            onResult.invoke(jsonObject.toString(), datePicker.date)
            dismiss()
        }
    }

}
