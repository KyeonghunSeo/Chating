package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
import com.hellowo.journey.ui.dialog.ColorPickerDialog
import com.hellowo.journey.model.ColorTag
import com.hellowo.journey.dpToPx
import com.hellowo.journey.ui.activity.MainActivity
import kotlinx.android.synthetic.main.list_item_color_picker.view.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    var items = ArrayList<ColorTag>()
    var selectedPos = -1
    lateinit var onSelceted: (Int, Int) -> Unit
    lateinit var setDialog: ColorPickerDialog

    init {
        MainActivity.instance?.viewModel?.colorTagList?.value?.let {
            items.clear()
            items.addAll(it)
        }
        layoutManager = GridLayoutManager(context, 4)
        adapter = PickerAdapter()
        addItemDecoration(SpacesItemDecoration(dpToPx(5)))
    }

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView
            v.titleText.text = item.title
            v.titleText.setTextColor(item.fontColor)
            (v as CardView).setCardBackgroundColor(item.color)

            v.setOnClickListener {
                selectedPos = position
                onSelceted.invoke(item.color, item.fontColor)
                setDialog.dismiss()
            }
        }
    }

    inner class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.top = space
            outRect.right = space
            outRect.bottom = space

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
            } else {
                //outRect.top = 0
            }
        }
    }
}