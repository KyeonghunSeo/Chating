package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.toast
import com.ayaan.twelvepages.ui.activity.RecordActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_bottom_template.view.*


class TemplateDialog : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_bottom_template)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
    }

}
