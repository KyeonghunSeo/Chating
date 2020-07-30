package com.ayaan.twelvepages.ui.sheet

import android.app.Dialog
import androidx.fragment.app.FragmentActivity
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.ColorPickerDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.sheet_edit_date_bg.view.*

class EditDateBgSheet(private val record: Record, private val onResult: (Boolean) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_edit_date_bg)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
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
