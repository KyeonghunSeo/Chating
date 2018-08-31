package com.hellowo.chating.calendar.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.TimeObject.Type
import kotlinx.android.synthetic.main.list_item_type_picker.view.*

class TypePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = PickerAdapter()
    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = Type.values().size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_type_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val type = Type.values()[position]
            val v = holder.itemView
            v.titleText.text = context.getString(type.titleId)
            v.iconImg.setImageResource(type.iconId)
            v.setOnClickListener {

            }
        }
    }
}