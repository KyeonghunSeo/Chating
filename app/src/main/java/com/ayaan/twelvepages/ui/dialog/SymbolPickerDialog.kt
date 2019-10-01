package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import android.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.SymbolListAdapter
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.manager.SymbolManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_bottom_sheet_normal_list.view.*


class SymbolPickerDialog(private val symbol: String?, private val onResult: (SymbolManager.Symbol) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_bottom_sheet_normal_list)
        sheetBehavior.peekHeight = dpToPx(350)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        root.recyclerView.layoutManager = GridLayoutManager(context, 8)
        root.recyclerView.adapter = SymbolListAdapter(context!!) { v, symbol, action ->
            onResult.invoke(symbol)
            dismiss()
        }
    }

}
