package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import kotlinx.android.synthetic.main.container_date_picker_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*


class DatePickerDialog(activity: Activity, time: Long, private val onResult: (Long) -> Unit) : BaseDialog(activity) {
    private val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_date_picker_dlg, dpToPx(320))
        setLayout()
    }

    private fun setLayout() {
        hideHeader()
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

    fun setMinDate(minDate: Long) {
        datePicker.minDate = minDate
    }

}
