package com.hellowo.chating.ui.listener

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.view.DragEvent
import android.view.DragEvent.*
import android.view.View
import com.hellowo.chating.DragMode
import com.hellowo.chating.calendar.view.CalendarView
import com.hellowo.chating.l
import com.hellowo.chating.ui.activity.MainActivity

object MainDragAndDropListener : View.OnDragListener {
    var dragMode = DragMode.NONE

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        event?.let { MainActivity.instance?.onDrag(event) }
        return true
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
        override fun onProvideShadowMetrics(size: Point, touch: Point) {}
        override fun onDrawShadow(canvas: Canvas) {
            //getView().draw(canvas);
        }
    }
}