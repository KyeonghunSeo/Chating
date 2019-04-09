package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.startDialogShowAnimation
import kotlinx.android.synthetic.main.dialog_date_picker.*
import java.util.*


class DatePickerDialog(activity: Activity, time: Long,
                       private val onResult: (Long) -> Unit) : Dialog(activity) {
    private val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_date_picker)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            cal.set(Calendar.YEAR, datePicker.year)
            cal.set(Calendar.MONTH, datePicker.month)
            cal.set(Calendar.DATE, datePicker.dayOfMonth)
            onResult.invoke(cal.timeInMillis)
            dismiss()
        }
    }

}
