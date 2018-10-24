package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.startDialogShowAnimation
import kotlinx.android.synthetic.main.dialog_custom.*


class CustomDialog(activity: Activity, private val title: String?, private val sub: String?,
                   private val options: Array<String>?, private val onResult: (Boolean, Int) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_custom)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        confirmBtn.setOnClickListener {
            onResult.invoke(true, 0)
            dismiss()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

}
