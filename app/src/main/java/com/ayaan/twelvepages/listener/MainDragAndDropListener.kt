package com.ayaan.twelvepages.listener

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.view.DragEvent
import android.view.View
import com.ayaan.twelvepages.ui.activity.MainActivity

object MainDragAndDropListener : View.OnDragListener {
    enum class DragMode { NONE, INSERT, MOVE }

    var dragMode = DragMode.NONE
    var startTime = Long.MIN_VALUE
    var currentTime = Long.MIN_VALUE

    fun start(view: View, dragMode: DragMode) {
        this.dragMode = dragMode
        val data = ClipData.newPlainText("dragType", "main")
        val shadowBuilder = BlankDragShadowBuilder(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadowBuilder, null, 0)
        } else {
            view.startDrag(data, shadowBuilder, null, 0)
        }
    }

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        return if(dragMode != DragMode.NONE) {
            event?.let {
                MainActivity.instance?.deliveryDragEvent(event)
                when(event.action) {
                    DragEvent.ACTION_DROP -> drop()
                    DragEvent.ACTION_DRAG_ENDED -> {
                        MainActivity.instance?.endDrag()
                        end()
                    }
                }
            }
            true
        }else {
            false
        }
    }

    fun drop() {
        if(dragMode == MainDragAndDropListener.DragMode.INSERT) {
            if(currentTime < startTime) {
                val t = startTime
                startTime = currentTime
                currentTime = t
            }
            MainActivity.instance?.expandControlView(startTime, currentTime)
        }
    }

    fun end() {
        this.dragMode = DragMode.NONE
        startTime = Long.MIN_VALUE
        currentTime = Long.MIN_VALUE
    }

    class BlankDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            size.set(1, 1)
            touch.set(0, 0)
        }
        override fun onDrawShadow(canvas: Canvas) {}
    }
}