package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.hellowo.journey.*
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.calendar.RepeatManager
import kotlinx.android.synthetic.main.dialog_repeat.*
import org.json.JSONObject
import java.util.*


class RepeatDialog(private val activity: Activity, private val timeObject: TimeObject,
                   private val onResult: (String?, Long) -> Unit) : Dialog(activity) {

    val jsonObject = JSONObject()
    var freq = 1
    var interval = 1
    var weekNum = StringBuilder("0000000")
    var monthOption = 0
    var dtUntil = Long.MIN_VALUE
    val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = timeObject.dtStart
        timeObject.repeat?.let {
            val repeatObject = JSONObject(it)
            freq = repeatObject.getInt("freq")
            interval = repeatObject.getInt("interval")
            weekNum.replace(0, 7, repeatObject.getString("weekNum"))
            monthOption = repeatObject.getInt("monthOption")
        }
        dtUntil = timeObject.dtUntil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_repeat)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        val freqBtns = arrayOf(dailyBtn, weeklyBtn, monthlyBtn, yearlyBtn)
        freqBtns.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                freq = index
                setFreqLy(freqBtns)
                setRepeatText()
            }
        }

        if(interval > 1 || weekNum.toString() != "0000000" || monthOption != 0 || dtUntil != Long.MIN_VALUE) {
            advancedBtn.visibility = View.GONE
            advancedLy.visibility = View.VISIBLE
        }else {
            advancedBtn.visibility = View.VISIBLE
            advancedLy.visibility = View.GONE
            advancedBtn.setOnClickListener {
                advancedBtn.visibility = View.GONE
                advancedLy.visibility = View.VISIBLE
            }
        }

        val weekNumChecks = arrayOf(sunCheck, monCheck, tueCheck, wedCheck, thuCheck, friCheck, satCheck)
        weekNumChecks.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                if(weekNum[index] == '0') {
                    weekNum.replace(index, index + 1, "1")
                }else {
                    weekNum.replace(index, index + 1, "0")
                }
                setWeekNumLy(weekNumChecks)
                setRepeatText()
            }
        }

        intervalEdit.setText(interval.toString())
        intervalEdit.setSelection(interval.toString().length)
        intervalEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setIntervalLy()
                setRepeatText()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        val date = cal.time
        monthlyText.text = String.format(context.getString(R.string.monthly_interval),
                cal.get(Calendar.DATE).toString())
        monthlyWText.text = String.format(context.getString(R.string.monthly_w_interval),
                String.format(context.getString(R.string.weekNum), cal.get(Calendar.WEEK_OF_MONTH).toString()),
                AppRes.dow.format(date))

        monthlyCheck.setOnClickListener {
            monthOption = 0
            setMonthlyLy()
            setRepeatText()
        }
        monthlyWCheck.setOnClickListener {
            monthOption = 1
            setMonthlyLy()
            setRepeatText()
        }

        untilCheck.setOnClickListener {
            dtUntil = if(dtUntil == Long.MIN_VALUE) {
                getCalendarTime23(cal) + WEEK_MILL * 4
            }else {
                Long.MIN_VALUE
            }
            setUntilLy()
            setRepeatText()
        }

        untilDateText.setOnClickListener {
            showDialog(DatePickerDialog(activity, dtUntil) { time ->
                dtUntil = time
                setUntilLy()
                setRepeatText()
            }, true, true, true, false)
        }

        confirmBtn.setOnClickListener {
            onResult.invoke(makeRepeatData(), dtUntil)
            dismiss()
        }

        deleteBtn.setOnClickListener {
            onResult.invoke(null, Long.MIN_VALUE)
            dismiss()
        }

        setFreqLy(freqBtns)
        setIntervalLy()
        setWeekNumLy(weekNumChecks)
        setMonthlyLy()
        setUntilLy()
        setRepeatText()
    }

    private fun setRepeatText() {
        titleText.text = RepeatManager.makeRepeatText(cal.timeInMillis, freq, interval, weekNum.toString(), monthOption, dtUntil)
    }

    private fun makeRepeatData() : String {
        jsonObject.put("freq", freq)
        val i = intervalEdit.text.toString().toInt()
        interval = if(i > 1) i else 1
        jsonObject.put("interval", interval)
        jsonObject.put("weekNum", weekNum.toString())
        jsonObject.put("monthOption", monthOption)
        return jsonObject.toString()
    }

    private fun setUntilLy() {
        if(dtUntil == Long.MIN_VALUE) {
            untilCheck.setImageResource(R.color.transparent)
            untilCheck.setBackgroundResource(R.drawable.normal_rect_stroke)
            untilDateText.visibility = View.GONE
            untilText.setTextColor(AppRes.secondaryText)
        }else {
            untilCheck.setImageResource(R.drawable.check_line)
            untilCheck.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
            untilDateText.visibility = View.VISIBLE
            untilText.setTextColor(AppRes.primaryText)
            untilDateText.text = AppRes.ymdDate.format(Date(dtUntil))
        }
    }

    private fun setMonthlyLy() {
        if(monthOption == 0) {
            monthlyCheck.setImageResource(R.drawable.check_line)
            monthlyCheck.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
            monthlyText.setTextColor(AppRes.primaryText)

            monthlyWCheck.setImageResource(R.color.transparent)
            monthlyWCheck.setBackgroundResource(R.drawable.normal_rect_stroke)
            monthlyWText.setTextColor(AppRes.secondaryText)
        }else {
            monthlyCheck.setImageResource(R.color.transparent)
            monthlyCheck.setBackgroundResource(R.drawable.normal_rect_stroke)
            monthlyText.setTextColor(AppRes.secondaryText)

            monthlyWCheck.setImageResource(R.drawable.check_line)
            monthlyWCheck.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
            monthlyWText.setTextColor(AppRes.primaryText)
        }
    }

    private fun setIntervalLy() {
        if(intervalEdit.text.isNotEmpty() && intervalEdit.text.toString().toInt() > 1) {
            interval = intervalEdit.text.toString().toInt()
            intervalEdit.setTextColor(Color.WHITE)
            intervalEdit.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
            intervalEdit.typeface = AppRes.regularFont
            intervalText.setTextColor(AppRes.primaryText)
        }else {
            interval = 0
            intervalEdit.setTextColor(AppRes.secondaryText)
            intervalEdit.setBackgroundResource(R.drawable.normal_rect_stroke)
            intervalEdit.typeface = AppRes.thinFont
            intervalText.setTextColor(AppRes.secondaryText)
        }
    }

    private fun setWeekNumLy(weekNumChecks: Array<TextView>) {
        weekNumChecks.forEachIndexed { index, textView ->
            if(weekNum[index] == '0') {
                textView.setTextColor(AppRes.secondaryText)
                textView.setBackgroundResource(R.drawable.normal_rect_stroke)
                textView.typeface = AppRes.thinFont
            }else {
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
                textView.typeface = AppRes.regularFont
            }
        }
    }

    private fun setFreqLy(freqBtns: Array<TextView>) {
        freqBtns.forEachIndexed { index, textView ->
            if(freq == index) {
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundResource(R.drawable.primary_rect_fill_radius_1)
                textView.typeface = AppRes.regularFont
            }else {
                textView.setTextColor(AppRes.secondaryText)
                textView.setBackgroundResource(R.color.transparent)
                textView.typeface = AppRes.thinFont
            }
        }

        when(freq) {
            0 -> {
                intervalLy.visibility = View.VISIBLE
                intervalText.text = String.format(context.getString(R.string.daily_interval), "")

                weeklyLy.visibility = View.GONE
                monthlyLy.visibility = View.GONE
                monthlyWLy.visibility = View.GONE
            }
            1 -> {
                intervalLy.visibility = View.VISIBLE
                intervalText.text = String.format(context.getString(R.string.weekly_interval), "")

                weeklyLy.visibility = View.VISIBLE
                monthlyLy.visibility = View.GONE
                monthlyWLy.visibility = View.GONE
            }
            2 -> {
                intervalLy.visibility = View.GONE
                weeklyLy.visibility = View.GONE

                monthlyLy.visibility = View.VISIBLE
                monthlyWLy.visibility = View.VISIBLE
            }
            3 -> {
                intervalLy.visibility = View.GONE
                weeklyLy.visibility = View.GONE
                monthlyLy.visibility = View.GONE
                monthlyWLy.visibility = View.GONE
            }
        }
    }

}
