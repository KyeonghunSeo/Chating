package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.ui.view.TimeObjectDetailView
import kotlinx.android.synthetic.main.dialog_more_option.*


class MoreOptionDialog(activity: Activity, private val timeObject: TimeObject,
                       private val timeObjectDetailView: TimeObjectDetailView) : Dialog(activity) {

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

        if(timeObject.isScheduled()) {
            scheduleBtn.alpha = 0.5f
        }else {
            scheduleBtn.alpha = 1f
            scheduleBtn.setOnClickListener {
                timeObjectDetailView.showStartEndDialog()
                dismiss()
            }
        }

        if(timeObject.isSetDday()) {
            ddayBtn.alpha = 0.5f
        }else {
            ddayBtn.alpha = 1f
            ddayBtn.setOnClickListener {
                timeObject.setDday()
                timeObjectDetailView.updateDdayUI()
                dismiss()
            }
        }

        memoBtn.setOnClickListener {
            timeObjectDetailView.showMemoUI()
            dismiss()
        }

        locationBtn.setOnClickListener {
            timeObjectDetailView.showPlacePicker()
            dismiss()
        }

        alarmBtn.setOnClickListener {
            timeObjectDetailView.addNewAlarm()
            dismiss()
        }

        repeatBtn.setOnClickListener {
            timeObjectDetailView.showRepeatDialog()
            dismiss()
        }

        if(timeObject.isSetCheckBox()) {
            checkboxBtn.alpha = 0.5f
        }else {
            checkboxBtn.alpha = 1f
            checkboxBtn.setOnClickListener {
                timeObject.setCheckBox()
                timeObjectDetailView.updateCheckBoxUI()
                dismiss()
            }
        }

        if(timeObject.isSetCheckList()) {
            checkListBtn.alpha = 0.5f
        }else {
            checkListBtn.alpha = 1f
            checkListBtn.setOnClickListener {
                timeObject.setCheckList()
                timeObjectDetailView.updateCheckListUI()
                dismiss()
            }
        }

        if(timeObject.isSetCheckList()) {
            checkListBtn.alpha = 0.5f
        }else {
            checkListBtn.alpha = 1f
            checkListBtn.setOnClickListener {
                timeObject.setCheckList()
                timeObjectDetailView.updateCheckListUI()
                dismiss()
            }
        }

        if(timeObject.isSetDeadLine()) {
            deadlineBtn.alpha = 0.5f
        }else {
            deadlineBtn.alpha = 1f
            deadlineBtn.setOnClickListener {
                timeObject.setDeadLine()
                timeObjectDetailView.updateDeadLineUI()
                dismiss()
            }
        }

        if(timeObject.isSetPercentage()) {
            percentageBtn.alpha = 0.5f
        }else {
            percentageBtn.alpha = 1f
            percentageBtn.setOnClickListener {
                timeObject.setPercentage()
                timeObjectDetailView.updatePercentageUI()
                dismiss()
            }
        }

        tagBtn.setOnClickListener {
            timeObjectDetailView.showTagDialog()
            dismiss()
        }

        photoBtn.setOnClickListener {
            timeObjectDetailView.showImagePicker()
            dismiss()
        }

        webLinkBtn.setOnClickListener {
            timeObjectDetailView.showEditWebsiteDialog()
            dismiss()
        }

        cancelBtn.setOnClickListener { dismiss() }
    }

}
