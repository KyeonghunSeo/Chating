package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import kotlinx.android.synthetic.main.list_item_color_picker.view.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var items = AppTheme.colors
    var selectedPos = -1
    lateinit var onSelceted: (Int) -> Unit

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = PickerAdapter()
        //addItemDecoration(SpacesItemDecoration(dpToPx(5)))
    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {}
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val color = items[position]
            val v = holder.itemView
            v.colorImg.setColorFilter(color)
            v.setOnClickListener {
                selectedPos = position
                onSelceted.invoke(position)
            }
        }
    }
}