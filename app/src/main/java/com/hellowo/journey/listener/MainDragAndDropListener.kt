package com.hellowo.journey.listener

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.view.DragEvent
import android.view.View
import com.hellowo.journey.DragMode
import com.hellowo.journey.ui.activity.MainActivity

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