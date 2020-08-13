package com.ayaan.twelvepages.ui.sheet

import android.app.Dialog
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.ColorPickerDialog
import com.ayaan.twelvepages.ui.dialog.SchedulingDialog
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
        root.rootLy.setOnClickListener {}
        val cal = Calendar.getInstance()
        root.previewDateText.text = cal.get(Calendar.DATE).toString()
        root.dateBgStylePicker.selectedPos = record.getBgLink()?.intParam0 ?: 0
        root.dateBgStylePicker.adapter?.notifyDataSetChanged()
        root.dateBgStylePicker.onSelected = {
            record.getBgLink()?.intParam0 = it
            drawSample()
        }

        root.areaLy.setOnClickListener {
            record.getBgLink()?.intParam2 = when(record.getBgLink()?.intParam2) {
                0 -> 1
                1 -> 2
                else -> 0
            }
            drawSample()
        }

        root.alphaLy.setOnClickListener {
            record.getBgLink()?.intParam1 = when(record.getBgLink()?.intParam1) {
                0 -> 1
                1 -> 2
                else -> 0
            }
            drawSample()
        }

        root.colorLy.setOnClickListener {
            ColorPickerDialog(record.colorKey){ colorKey ->
                record.colorKey = colorKey
                drawSample()
            }.show(activity!!.supportFragmentManager, null)
        }

        root.dateText.text = makeSheduleText(record.dtStart, record.dtEnd,
                false, false, false, true)
        root.dateLy.setOnClickListener {
            showDialog(SchedulingDialog(activity!!, record, 0) { sCal, eCal ->
                record.setDateTime(sCal, eCal)
                root.dateText.text = makeSheduleText(record.dtStart, record.dtEnd,
                        false, false, false, true)
            }, true, true, true, false)
        }

        root.cancelBtn.setOnClickListener { dismiss() }

        root.confirmBtn.setOnClickListener {
            onResult.invoke(true)
            dismiss()
        }

        drawSample()
    }

    private fun drawSample() {
        record.getBgLink()?.let {
            val alpha = it.intParam1
            root.alphaText.text = when(alpha) {
                0 -> getString(R.string.thin)
                1 -> getString(R.string.normal)
                else -> getString(R.string.bold)
            }
            val area = it.intParam2
            root.areaText.text = when(area) {
                0 -> getString(R.string.background_area_all)
                1 -> getString(R.string.background_area_bottom_ribbon)
                else -> getString(R.string.background_area_top_date)
            }
        }
        root.colorImg.setColorFilter(ColorManager.getColor(record.colorKey))
        root.dateBgSample.setDateBg(record)
    }

}
