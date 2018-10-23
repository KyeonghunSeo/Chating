package com.hellowo.journey.calendar.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellowo.journey.R
import com.hellowo.journey.calendar.model.Alarm
import com.hellowo.journey.calendar.model.TimeObject
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import java.util.*


@SuppressLint("ValidFragment")
class DateTimePickerDialog(private val title: String, private val time: Long,
                           private val onResult: (Long) -> Unit) : BottomSheetDialogFragment() {
    val cal = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = View.inflate(context, R.layout.dialog_date_time_picker, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.setOnShowListener { _ ->
            (rootLy.parent as View).setBackgroundColor(Color.TRANSPARENT)
            val behavior = ((rootLy.parent as View).layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior<*>?
            behavior?.let {
                it.state = BottomSheetBehavior.STATE_EXPANDED
                titleText.text = title
                cal.timeInMillis = time

            }
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        onResult.invoke(cal.timeInMillis)
    }
}
