package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.ui.activity.RecordActivity
import kotlinx.android.synthetic.main.dialog_more_option.*


class MoreOptionDialog(activity: Activity, private val record: Record,
                       private val recordActivity: RecordActivity) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_more_option)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        if(record.isScheduled()) {
            scheduleBtn.alpha = 0.5f
        }else {
            scheduleBtn.alpha = 1f
            scheduleBtn.setOnClickListener {
                recordActivity.showStartEndDialog()
                dismiss()
            }
        }

        if(record.isSetDday()) {
            ddayBtn.alpha = 0.5f
        }else {
            ddayBtn.alpha = 1f
            ddayBtn.setOnClickListener {
                record.setDday()
                recordActivity.updateDdayUI()
                dismiss()
            }
        }

        memoBtn.setOnClickListener {
            recordActivity.showMemoUI()
            dismiss()
        }

        locationBtn.setOnClickListener {
            recordActivity.showPlacePicker()
            dismiss()
        }

        alarmBtn.setOnClickListener {
            recordActivity.showAlarmDialog()
            dismiss()
        }

        if(record.isRepeat()) {
            repeatBtn.alpha = 0.5f
        }else {
            repeatBtn.alpha = 1f
            repeatBtn.setOnClickListener {
                recordActivity.showRepeatDialog()
                dismiss()
            }
        }

        if(record.isRepeat()) {
            lunarRepeatBtn.alpha = 0.5f
        }else {
            lunarRepeatBtn.alpha = 1f
            lunarRepeatBtn.setOnClickListener {
                recordActivity.showLunarRepeatDialog()
                dismiss()
            }
        }

        if(record.isSetCheckBox()) {
            checkboxBtn.alpha = 0.5f
        }else {
            checkboxBtn.alpha = 1f
            checkboxBtn.setOnClickListener {
                record.setCheckBox()
                recordActivity.updateCheckBoxUI()
                dismiss()
            }
        }

        if(record.isSetCheckList()) {
            checkListBtn.alpha = 0.5f
        }else {
            checkListBtn.alpha = 1f
            checkListBtn.setOnClickListener {
                record.setCheckList()
                recordActivity.updateCheckListUI()
                dismiss()
            }
        }

        if(record.isSetCheckList()) {
            checkListBtn.alpha = 0.5f
        }else {
            checkListBtn.alpha = 1f
            checkListBtn.setOnClickListener {
                record.setCheckList()
                recordActivity.updateCheckListUI()
                dismiss()
            }
        }

        if(record.isSetPercentage()) {
            percentageBtn.alpha = 0.5f
        }else {
            percentageBtn.alpha = 1f
            percentageBtn.setOnClickListener {
                record.setPercentage()
                recordActivity.updatePercentageUI()
                dismiss()
            }
        }

        tagBtn.setOnClickListener {
            recordActivity.showTagDialog()
            dismiss()
        }

        photoBtn.setOnClickListener {
            recordActivity.showImagePicker()
            dismiss()
        }

        webLinkBtn.setOnClickListener {
            recordActivity.showEditWebsiteDialog()
            dismiss()
        }
    }

}
