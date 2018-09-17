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
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.list_item_color_picker.view.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    var items: List<ColorTag>
    var selectedPos = -1

    init {
        layoutManager =  GridLayoutManager(context, 4)

        val realm = Realm.getDefaultInstance()
        items = realm.where(ColorTag::class.java).sort("order", Sort.ASCENDING).findAll()
        if(items.isEmpty()) {
            realm.executeTransaction {
                var id = 0
                context.resources.getStringArray(R.array.colors).forEach {
                    val note = realm.createObject(ColorTag::class.java, id)
                    note.title = "_"
                    note.color = Color.parseColor(it)
                    note.order = id
                    id++
                }
            }
            items = realm.where(ColorTag::class.java).sort("order", Sort.ASCENDING).findAll()
        }
        realm.close()

        adapter = PickerAdapter()
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