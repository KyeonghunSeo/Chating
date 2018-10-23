package com.hellowo.journey.calendar.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.startDialogShowAnimation
import com.hellowo.journey.startFromBottomSlideAppearAnimation
import kotlinx.android.synthetic.main.dialog_date_picker.*
import java.util.*


class DatePickerDialog(private val activity: Activity, private val time: Long,
                       private val onResult: (Long) -> Unit) : Dialog(activity) {
    val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_date_picker)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy, dpToPx(10).toFloat())
        }
    }

    private fun setLayout() {
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
