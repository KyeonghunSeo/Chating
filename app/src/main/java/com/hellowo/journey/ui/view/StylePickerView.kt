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
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.model.TimeObject.Type
import kotlinx.android.synthetic.main.list_item_type_picker.view.*

class StylePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    var timeObject: TimeObject? = null
    var selectedPos = -1

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = PickerAdapter()
    }

    fun setTypeObject(timeObject: TimeObject) {
        this.timeObject = timeObject
        selectedPos = timeObject.type
        adapter?.notifyDataSetChanged()
    }

    fun refresh() {

    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = Type.values().size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                itemView.titleText.setTextColor(Color.WHITE)
                itemView.iconImg.setColorFilter(Color.WHITE)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_type_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val type = Type.values()[position]
            val v = holder.itemView
            v.titleText.text = context.getString(type.titleId)
            v.iconImg.setImageResource(type.iconId)

            timeObject?.let {
                if(type.ordinal == it.type) {
                    v.rootLy.setCardBackgroundColor(it.color)
                }else {
                    v.rootLy.setCardBackgroundColor(AppTheme.disableText)
                }
            }

            v.setOnClickListener {
                timeObject?.type = type.ordinal
                if(selectedPos >= 0) {
                    notifyItemChanged(selectedPos)
                }
                notifyItemChanged(position)
                selectedPos = position
            }
        }
    }
}