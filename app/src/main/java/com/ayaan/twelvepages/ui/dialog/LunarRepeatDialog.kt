package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.KoreanLunarCalendar
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.startDialogShowAnimation
import kotlinx.android.synthetic.main.dialog_lunar_repeat.*
import org.json.JSONObject
import java.util.*


class LunarRepeatDialog(activity: Activity, record: Record,
                        private val onResult: (String?, Long) -> Unit) : Dialog(activity) {
    private val cal = Calendar.getInstance()
    private val lunarCal = KoreanLunarCalendar.getInstance()

    init {
        cal.timeInMillis = record.dtStart
        lunarCal.setSolarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_lunar_repeat)
        setLayout()
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()
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
