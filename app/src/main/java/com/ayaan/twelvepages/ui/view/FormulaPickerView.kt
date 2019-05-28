package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.list_item_tab.view.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*

class FormulaPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val items = context.resources.getStringArray(R.array.formulas)
    private val formulas = arrayOf(STACK, EXPANDED, RANGE, IMAGE, DOT)

    var onSelected : ((RecordCalendarAdapter.Formula) -> Unit)? = null
    var formula = STACK

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = Adapter()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                container.layoutParams.width = WRAP_CONTENT
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            v.titleText.text = items[position]
            v.iconImg.setImageResource(R.drawable.menu)

            if(formulas[position] == formula) {
                v.titleText.setTextColor(Color.WHITE)
                v.titleText.typeface = AppTheme.boldFont
                v.contentLy.setBackgroundColor(AppTheme.primaryColor)
                v.contentLy.alpha = 1f
            }else {
                v.titleText.setTextColor(AppTheme.primaryColor)
                v.titleText.typeface = AppTheme.regularFont
                v.contentLy.setBackgroundResource(R.drawable.normal_rect_stroke)
                v.contentLy.alpha = 0.4f
            }

            v.setOnClickListener {
                if(formulas[position] != formula) {
                    notifyItemChanged(formulas.indexOf(formula))
                    notifyItemChanged(position)
                    formula = formulas[position]
                    onSelected?.invoke(formula)
                }
            }
        }
    }
}