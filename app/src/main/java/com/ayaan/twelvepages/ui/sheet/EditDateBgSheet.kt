package com.ayaan.twelvepages.ui.sheet

import android.app.Dialog
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.ayaan.twelvepages.AppDateFormat
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.ColorPickerDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.container_in_calendar_style_dlg.*
import kotlinx.android.synthetic.main.sheet_edit_date_bg.view.*
import java.util.*

class EditDateBgSheet(private val record: Record, private val onResult: (Boolean) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_edit_date_bg)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        root.setOnClickListener {}
        val cal = Calendar.getInstance()
        root.dateText.text = cal.get(Calendar.DATE).toString()
        root.dowText.text = AppDateFormat.dow.format(cal.time)
        root.dowText.visibility = View.GONE
        root.holiText.visibility = View.GONE
        cal.add(Calendar.DATE, 1)
        root.dateText2.text = cal.get(Calendar.DATE).toString()
        cal.add(Calendar.DATE, 1)
        root.dateText3.text = cal.get(Calendar.DATE).toString()
        cal.add(Calendar.DATE, 1)
        root.dateText4.text = cal.get(Calendar.DATE).toString()
        cal.add(Calendar.DATE, 1)
        root.dateText5.text = cal.get(Calendar.DATE).toString()

        root.dateBgStylePicker.selectedPos = record.getBgLink()?.intParam0 ?: 0
        root.dateBgStylePicker.adapter?.notifyDataSetChanged()
        root.dateBgStylePicker.onSelected = {
            record.getBgLink()?.intParam0 = it
            drawSample()
        }

        root.colorImg.setOnClickListener {
            ColorPickerDialog(record.colorKey){ colorKey ->
                record.colorKey = colorKey
                drawSample()
            }.show(activity!!.supportFragmentManager, null)
        }

        root.cancelBtn.setOnClickListener { dismiss() }

        root.confirmBtn.setOnClickListener {
            onResult.invoke(true)
            dismiss()
        }

        drawSample()
    }

    private fun drawSample() {
        root.colorImg.setColorFilter(ColorManager.getColor(record.colorKey))
        root.dateBgSample.setDateBg(record)
    }

}
