package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.removeDimStatusBar
import com.ayaan.twelvepages.toast
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.view_clipboard.view.*

@SuppressLint("ClickableViewAccessibility")
class ClipboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private var behavior: BottomSheetBehavior<View>

    init {
        LayoutInflater.from(context).inflate(R.layout.view_clipboard, this, true)
        setOnTouchListener { _, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                if(MainActivity.isProfileOpened()) {
                    MainActivity.closeProfileView()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener super.onTouchEvent(motionEvent)
        }

        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.skipCollapsed = true
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                    hiddened()
                }
            }
        })
    }

    fun clip(record: Record?) {
        if(record == null) {
            collapse()
        }else {
            clipText.text = record.getTitleInCalendar()
            clipIconImg.setColorFilter(record.getColor())
            clipPasteBtn.setOnClickListener {
                MainActivity.getTargetFolder().let { record.folder = Folder(it) }
                record.moveDate(MainActivity.getTargetCal())
                if(record.id.isNullOrEmpty()) {
                    if(record.isRepeat()) {
                        record.clearRepeat()
                    }
                    RecordManager.save(record)
                    toast(R.string.copied, R.drawable.copy)
                }else {
                    if(record.isRepeat()) {
                        RecordManager.deleteOnly(record)
                        record.clearRepeat()
                        record.id = null
                        RecordManager.save(record)
                    }else {
                        RecordManager.delete(record)
                        record.id = null
                        RecordManager.save(record)
                    }
                    toast(R.string.moved, R.drawable.change)
                }
                MainActivity.getViewModel()?.clipRecord?.value = null
            }
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun collapse() {
        MainActivity.instance?.window?.let { removeDimStatusBar(it) }
        MainActivity.instance?.clearCalendarHighlight()
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun hiddened() {
        MainActivity.instance?.window?.let { removeDimStatusBar(it) }
        MainActivity.instance?.clearCalendarHighlight()
    }

    fun notifyListChanged() {}

    fun isExpanded() = behavior.state != BottomSheetBehavior.STATE_HIDDEN
}