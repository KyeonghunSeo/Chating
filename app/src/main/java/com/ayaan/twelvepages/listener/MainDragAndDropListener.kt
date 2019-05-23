package com.ayaan.twelvepages.listener

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.view.DragEvent
import android.view.View
import com.ayaan.twelvepages.ui.activity.MainActivity

object MainDragAndDropListener : View.OnDragListener {
    enum class DragMode {
        NONE, INSERT, MOVE
    }

    var dragMode = DragMode.NONE

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        return if(dragMode != DragMode.NONE) {
            event?.let { MainActivity.instance?.onDrag(event) }
            true
        }else {
            false
        }
    }

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

    fun end() {
        this.dragMode = DragMode.NONE
    }

    class BlankDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            size.set(1, 1)
            touch.set(0, 0)
        }
        override fun onDrawShadow(canvas: Canvas) {}
    }
}