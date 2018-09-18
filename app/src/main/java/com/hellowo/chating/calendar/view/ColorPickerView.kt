package com.hellowo.chating.calendar.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.chating.App
import com.hellowo.chating.AppRes
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.model.ColorTag
import com.hellowo.chating.calendar.model.Template
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.model.TimeObject.Type
import com.hellowo.chating.ui.activity.MainActivity
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.list_item_color_picker.view.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    var items = ArrayList<ColorTag>()
    var selectedPos = -1

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = PickerAdapter()
    }

    fun show() {
        MainActivity.instance?.viewModel?.colorTagList?.value?.let {
            items.clear()
            items.addAll(it)
            adapter?.notifyDataSetChanged()
            visibility = View.VISIBLE
        }
    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                itemView.titleText.typeface = CalendarSkin.dateFont
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView
            v.titleText.text = item.title
            v.setBackgroundColor(item.color)

            v.setOnClickListener {
                selectedPos = position
            }
        }
    }
}