package com.hellowo.journey.calendar.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.View
import androidx.cardview.widget.CardView
import com.google.android.gms.location.places.ui.PlacePicker
import com.hellowo.journey.R
import com.hellowo.journey.RC_FILEPICKER
import com.hellowo.journey.RC_LOCATION
import com.hellowo.journey.calendar.model.TimeObject
import com.hellowo.journey.calendar.view.ColorPickerView
import com.hellowo.journey.calendar.view.StylePickerView
import com.hellowo.journey.calendar.view.TypePickerView
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.BottomSheetDialog



@SuppressLint("ValidFragment")
class AddMoreOptionDialog(private val onResult: (Boolean) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_add_more_options, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            dialog.setOnShowListener {
                sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            (contentView.parent as View).setBackgroundColor(Color.TRANSPARENT)

            val locationBtn = contentView.findViewById<CardView>(R.id.locationBtn)
            locationBtn.setOnClickListener {
                val builder = PlacePicker.IntentBuilder()
                MainActivity.instance?.startActivityForResult(builder.build(MainActivity.instance), RC_LOCATION)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        onResult.invoke(true)
    }
}
