package com.hellowo.journey.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.View
import com.hellowo.journey.R
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.view.ColorPickerView
import com.hellowo.journey.ui.view.StylePickerView
import com.hellowo.journey.ui.view.TypePickerView


@SuppressLint("ValidFragment")
class TypePickerDialog(private val timeObject: TimeObject, private val onResult: (Boolean) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_type_picker, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            dialog.setOnShowListener {
                sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            (contentView.parent as View).setBackgroundColor(Color.TRANSPARENT)

            val typePicker = contentView.findViewById<TypePickerView>(R.id.typePicker)
            val stylePicker = contentView.findViewById<StylePickerView>(R.id.stylePicker)
            val colorPicker = contentView.findViewById<ColorPickerView>(R.id.colorPicker)
            typePicker.setTypeObject(timeObject)
            typePicker.onSelected = {
                timeObject.type = it.ordinal
                stylePicker.refresh()
            }
            stylePicker.setTypeObject(timeObject)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        onResult.invoke(true)
    }
}
