package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.model.TimeObject.Type
import kotlinx.android.synthetic.main.list_item_type_picker.view.*

class TypePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var selectedPos = -1
    var onSelected : ((Type) -> Unit)? = null
    var mode = 0

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = PickerAdapter()
    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = Type.values().size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                itemView.titleText.typeface = AppTheme.boldFont
                itemView.subText.typeface = AppTheme.thinFont
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_type_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val type = Type.values()[position]
            val v = holder.itemView

            if(mode == 0) {
                v.layoutParams.width = dpToPx(100)
                v.layoutParams.height = dpToPx(100)
                v.subText.visibility = View.GONE
            }else {
                v.layoutParams.width = dpToPx(180)
                v.layoutParams.height = dpToPx(180)
                v.subText.visibility = View.VISIBLE
            }

            v.titleText.text = context.getString(type.titleId)
            v.subText.text = context.getString(type.subTextId)
            v.typeImg.setImageResource(type.iconId)

            v.setOnClickListener {
                selectedPos = position
                onSelected?.invoke(type)
            }
        }
    }
}