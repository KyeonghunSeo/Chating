package com.hellowo.chating.calendar.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.model.TimeObject.Type
import com.hellowo.chating.dpToPx
import com.hellowo.chating.l
import kotlinx.android.synthetic.main.list_item_type_picker.view.*

class SelectedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val lineWidth = dpToPx(2).toFloat()
    private val lineLength = dpToPx(5).toFloat()
    private val paint = Paint()
    var color = Color.BLACK

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selected_bar, this, true)
        setWillNotDraw(false)
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.strokeWidth = lineWidth
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawLine(0f, 0f, lineLength, 0f, paint)
            it.drawLine(0f, 0f, 0f, lineLength, paint)
            it.drawLine(width.toFloat(), 0f, width - lineLength, 0f, paint)
            it.drawLine(width.toFloat(), 0f, width.toFloat(), lineLength, paint)
            it.drawLine(width.toFloat(), height.toFloat(), width - lineLength, height.toFloat(), paint)
            it.drawLine(width.toFloat(), height.toFloat(), width.toFloat(), height - lineLength, paint)
            it.drawLine(0f, height.toFloat(), 0f, height - lineLength, paint)
            it.drawLine(0f, height.toFloat(), lineLength, height.toFloat(), paint)
        }
        super.onDraw(canvas)
    }
}