package com.hellowo.chating.ui.listener

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.view.DragEvent
import android.view.DragEvent.*
import android.view.View
import com.hellowo.chating.l

object MainDragAndDropListener : View.OnDragListener {

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        when(event?.action) {
            ACTION_DRAG_STARTED -> {

            }
            ACTION_DRAG_LOCATION -> {
                l(""+event.x)
            }
            ACTION_DROP -> {

            }
            ACTION_DRAG_ENDED -> {

            }
        }
        return true
    }

    fun start(view: View) {
        val data = ClipData.newPlainText("dragType", "main")
        val shadowBuilder = BlankDragShadowBuilder(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadowBuilder, null, 0)
        } else {
            view.startDrag(data, shadowBuilder, null, 0)
        }
    }

    class BlankDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {
        override fun onProvideShadowMetrics(size: Point, touch: Point) {}
        override fun onDrawShadow(canvas: Canvas) {
            //getView().draw(canvas);
        }
    }
}