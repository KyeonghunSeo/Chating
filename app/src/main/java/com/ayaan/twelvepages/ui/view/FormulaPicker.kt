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
import kotlinx.android.synthetic.main.list_item_chip.view.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*

class FormulaPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val formulas = arrayOf(SINGLE_TEXT, MULTI_TEXT, BOTTOM_SINGLE_TEXT, DOT, BACKGROUND_TEXT)

    var onSelected : ((RecordCalendarAdapter.Formula) -> Unit)? = null
    var formula = SINGLE_TEXT

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = Adapter()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = formulas.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                container.layoutParams.width = WRAP_CONTENT
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_chip, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            val item = formulas[position]
            v.titleText.text = context.getString(item.nameId)

            if(item == formula) {
                v.titleText.setTextColor(Color.WHITE)
                v.titleText.typeface = AppTheme.boldFont
                v.contentLy.setBackgroundColor(AppTheme.secondaryText)
                v.contentLy.alpha = 1f
            }else {
                v.titleText.setTextColor(AppTheme.secondaryText)
                v.titleText.typeface = AppTheme.regularFont
                v.contentLy.setBackgroundColor(AppTheme.lightLine)
                v.contentLy.alpha = 0.4f
            }

            v.setOnClickListener {
                if(item != formula) {
                    notifyItemChanged(formulas.indexOf(formula))
                    notifyItemChanged(position)
                    formula = formulas[position]
                    onSelected?.invoke(formula)
                }
            }
        }
    }
}