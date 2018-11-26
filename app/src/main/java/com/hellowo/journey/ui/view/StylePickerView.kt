package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.model.TimeObject.Type
import kotlinx.android.synthetic.main.list_item_style_picker.view.*
import java.util.*

class StylePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    val cal = Calendar.getInstance()
    var selectedPos = -1
    var onSelected : ((Int) -> Unit)? = null
    var type = 0
    var colorKey = 0

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = StyleAdapter()
    }

    inner class StyleAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = Type.values()[type].styleCount

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                itemView.layoutParams.height = dpToPx(110)
                val timeObjectView = TimeObjectView(context, TimeObject(), 0, 0)
                timeObjectView.timeObject.title = context.getString(R.string.contents_example)
                timeObjectView.scaleX = 1.5f
                timeObjectView.scaleY = 1.5f
                timeObjectView.pivotX = 0.5f
                timeObjectView.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                itemView.previewContainer.addView(timeObjectView, 0)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_style_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView

            (v.previewContainer.getChildAt(0) as TimeObjectView).let {
                it.timeObject.type = type
                it.timeObject.style = position
                it.timeObject.colorKey = colorKey
                it.setLookByType()

                when(it.timeObject.type) {
                    2 -> { // 노트
                        it.layoutParams.width = dpToPx(60)
                        it.layoutParams.height = WRAP_CONTENT
                    }
                    4 -> { // 기간
                        it.layoutParams.width = dpToPx(120)
                        it.layoutParams.height = TimeObjectView.blockTypeSize
                    }
                    else -> {
                        it.layoutParams.width = dpToPx(60)
                        it.layoutParams.height = TimeObjectView.blockTypeSize
                    }
                }
                v.layoutParams.width = (it.layoutParams.width * 1.5f + dpToPx(41)).toInt()

                it.pivotY = it.getViewHeight() / 2f
                it.textSpaceWidth = it.paint.measureText(it.text.toString())
                it.invalidate()
                v.requestLayout()
            }

            v.setOnClickListener {
                selectedPos = position
                onSelected?.invoke(position)
            }
        }
    }
}