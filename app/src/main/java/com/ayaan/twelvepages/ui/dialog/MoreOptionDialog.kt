package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.RecordActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_more_option.view.*


class MoreOptionDialog(private val record: Record,
                       private val recordActivity: RecordActivity) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_more_option)
        sheetBehavior.peekHeight = dpToPx(350)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        if(record.isScheduled()) {
            root.scheduleBtn.alpha = 0.5f
        }else {
            root.scheduleBtn.alpha = 1f
            root.scheduleBtn.setOnClickListener {
                recordActivity.showStartEndDialog(0)
                dismiss()
            }
        }

        if(record.isSetCountdown()) {
            root.ddayBtn.alpha = 0.5f
        }else {
            root.ddayBtn.alpha = 1f
            root.ddayBtn.setOnClickListener {
                record.setCountdown()
                recordActivity.updateDdayUI()
                dismiss()
            }
        }

        root.memoBtn.setOnClickListener {
            recordActivity.showMemoUI()
            dismiss()
        }

        root.locationBtn.setOnClickListener {
            recordActivity.showPlacePicker()
            dismiss()
        }

        root.alarmBtn.setOnClickListener {
            recordActivity.showAlarmDialog()
            dismiss()
        }

        if(record.isRepeat()) {
            root.repeatBtn.alpha = 0.5f
        }else {
            root.repeatBtn.alpha = 1f
            root.repeatBtn.setOnClickListener {
                recordActivity.showRepeatDialog()
                dismiss()
            }
        }

        if(record.isRepeat()) {
            root.lunarRepeatBtn.alpha = 0.5f
        }else {
            root.lunarRepeatBtn.alpha = 1f
            root.lunarRepeatBtn.setOnClickListener {
                recordActivity.showLunarRepeatDialog()
                dismiss()
            }
        }

        if(record.isSetCheckBox) {
            root.checkboxBtn.alpha = 0.5f
        }else {
            root.checkboxBtn.alpha = 1f
            root.checkboxBtn.setOnClickListener {
                record.isSetCheckBox = true
                recordActivity.updateCheckBoxUI()
                dismiss()
            }
        }

        if(record.isSetCheckList()) {
            root.checkListBtn.alpha = 0.5f
        }else {
            root.checkListBtn.alpha = 1f
            root.checkListBtn.setOnClickListener {
                record.setCheckList()
                recordActivity.updateCheckListUI()
                dismiss()
            }
        }

        if(record.isSetCheckList()) {
            root.checkListBtn.alpha = 0.5f
        }else {
            root.checkListBtn.alpha = 1f
            root.checkListBtn.setOnClickListener {
                record.setCheckList()
                recordActivity.updateCheckListUI()
                dismiss()
            }
        }

        if(record.isSetPercentage()) {
            root.percentageBtn.alpha = 0.5f
        }else {
            root.percentageBtn.alpha = 1f
            root.percentageBtn.setOnClickListener {
                record.setPercentage()
                recordActivity.updatePercentageUI()
                dismiss()
            }
        }

        root.tagBtn.setOnClickListener {
            recordActivity.showTagDialog()
            dismiss()
        }

        root.photoBtn.setOnClickListener {
            recordActivity.showImagePicker()
            dismiss()
        }

        root.webLinkBtn.setOnClickListener {
            recordActivity.showEditWebsiteDialog()
            dismiss()
        }
    }

}
