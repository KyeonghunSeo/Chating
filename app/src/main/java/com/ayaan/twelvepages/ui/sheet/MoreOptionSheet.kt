package com.ayaan.twelvepages.ui.sheet

import android.app.Dialog
import com.ayaan.twelvepages.AppStatus
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.showPremiumDialog
import com.ayaan.twelvepages.toast
import com.ayaan.twelvepages.ui.activity.RecordActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_more_option.view.*


class MoreOptionSheet(private val record: Record,
                      private val recordActivity: RecordActivity) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_more_option)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        if(record.isSetTitle()) {
            root.titleBtn.alpha = 0.4f
            root.titleBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.titleBtn.alpha = 1f
            root.titleBtn.setOnClickListener {
                recordActivity.showTitleUI()
                dismiss()
            }
        }

//        if(record.isSetSymbol()) {
//            root.symbolBtn.alpha = 0.4f
//            root.symbolBtn.setOnClickListener { toast(R.string.already_active_info) }
//        }else {
//            root.symbolBtn.alpha = 1f
//            root.symbolBtn.setOnClickListener {
//                recordActivity.showSymbolDialog()
//                dismiss()
//            }
//        }

        if(record.isScheduled()) {
            root.scheduleBtn.alpha = 0.4f
            root.scheduleBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.scheduleBtn.alpha = 1f
            root.scheduleBtn.setOnClickListener {
                recordActivity.showStartEndDialog(0)
                dismiss()
            }
        }

        if(record.isSetCountdown()) {
            root.ddayBtn.alpha = 0.4f
            root.ddayBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.ddayBtn.alpha = 1f
            root.ddayBtn.setOnClickListener {
                recordActivity.showCountDownDialog()
                recordActivity.updateDdayUI()
                dismiss()
            }
        }

        if(record.isSetMainText()) {
            root.memoBtn.alpha = 0.4f
            root.memoBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.memoBtn.alpha = 1f
            root.memoBtn.setOnClickListener {
                recordActivity.showMemoUI()
                dismiss()
            }
        }

        if(record.isSetLocation()) {
            root.locationBtn.alpha = 0.4f
            root.locationBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.locationBtn.alpha = 1f
            root.locationBtn.setOnClickListener {
                recordActivity.showPlacePicker()
                dismiss()
            }
        }

        if(record.isSetAlarm()) {
            root.alarmBtn.alpha = 0.4f
            root.alarmBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.alarmBtn.alpha = 1f
            root.alarmBtn.setOnClickListener {
                recordActivity.showAlarmDialog()
                dismiss()
            }
        }

        if(record.isRepeat()) {
            root.repeatBtn.alpha = 0.4f
            root.repeatBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.repeatBtn.alpha = 1f
            root.repeatBtn.setOnClickListener {
                recordActivity.showRepeatDialog()
                dismiss()
            }
        }

        if(record.isRepeat()) {
            root.lunarRepeatBtn.alpha = 0.4f
            root.lunarRepeatBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.lunarRepeatBtn.alpha = 1f
            root.lunarRepeatBtn.setOnClickListener {
                recordActivity.showLunarRepeatDialog()
                dismiss()
            }
        }

        if(record.isSetCheckBox) {
            root.checkboxBtn.alpha = 0.4f
            root.checkboxBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.checkboxBtn.alpha = 1f
            root.checkboxBtn.setOnClickListener {
                record.isSetCheckBox = true
                recordActivity.updateCheckBoxUI()
                dismiss()
            }
        }

        if(record.isSetCheckList()) {
            root.checkListBtn.alpha = 0.4f
            root.checkListBtn.setOnClickListener { toast(R.string.already_active_info) }
        }else {
            root.checkListBtn.alpha = 1f
            root.checkListBtn.setOnClickListener {
                record.setCheckList()
                recordActivity.updateCheckListUI()
                dismiss()
            }
        }

        /*
        if(record.isSetPercentage()) {
            root.percentageBtn.alpha = 0.4f
        }else {
            root.percentageBtn.alpha = 1f
            root.percentageBtn.setOnClickListener {
                record.setPercentage()
                recordActivity.updatePercentageUI()
                dismiss()
            }
        }
*/

        root.tagBtn.setOnClickListener {
            recordActivity.showTagDialog()
            dismiss()
        }

        root.photoBtn.setOnClickListener {
            if(AppStatus.isPremium()) {
                recordActivity.showImagePicker()
                dismiss()
            }else {
                showPremiumDialog(recordActivity)
            }
        }

        root.webLinkBtn.setOnClickListener {
            recordActivity.showEditWebsiteDialog()
            dismiss()
        }
    }

}
