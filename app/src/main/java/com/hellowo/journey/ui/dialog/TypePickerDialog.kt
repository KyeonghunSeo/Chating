package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.transition.TransitionManager
import com.hellowo.journey.R
import com.hellowo.journey.makeChangeBounceTransition
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.dialog_type_picker.*

class TypePickerDialog(activity: Activity, private val type: TimeObject.Type,
                       private val onResult: (TimeObject.Type) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.gravity = Gravity.BOTTOM
        setContentView(R.layout.dialog_type_picker)
        setLayout()
        setOnShowListener {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setLayout() {
        rootLy.setBackgroundColor(Color.WHITE)
        detailBtn.setOnClickListener {
            if(typePicker.mode == 0) {
                typePicker.mode = 1
                detailBtn.text = context.getString(R.string.simple)
            }else {
                typePicker.mode = 0
                detailBtn.text = context.getString(R.string.detail)
            }
            TransitionManager.beginDelayedTransition(typePicker, makeChangeBounceTransition())
            typePicker.adapter?.notifyDataSetChanged()
        }
        typePicker.onSelected = onResult
    }

}
