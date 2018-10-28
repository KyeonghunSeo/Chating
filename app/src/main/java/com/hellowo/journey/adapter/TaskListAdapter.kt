package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.l
import com.hellowo.journey.model.CalendarSkin
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.repeat.RepeatManager
import kotlinx.android.synthetic.main.list_item_task.view.*
import java.util.*

class TaskListAdapter(val context: Context, val items: List<TimeObject>, val currentCal: Calendar,
                      val adapterInterface: (view: View?, timeObject: TimeObject, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            /*
            container.checkBox.imageAssetsFolder = "assets/"
            container.checkBox.setAnimation("checked_done.json")
            container.checkBox.visibility = View.GONE
            */
        }

        fun onItemSelected() {
            //itemView.setBackgroundColor(AppRes.almostWhite)
        }

        fun onItemClear() {
            //itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_task, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        /*
        v.checkBox.setOnClickListener {
            v.checkBox.playAnimation()
        }
        val filter = SimpleColorFilter(timeObject.color)
        val keyPath = KeyPath("**")
        val callback = LottieValueCallback<ColorFilter>(filter)
        v.checkBox.addValueCallback<ColorFilter>(keyPath, LottieProperty.COLOR_FILTER, callback)
        */

        v.titleText.text = if(timeObject.title.isNullOrBlank()) {
            context.getString(R.string.untitle)
        }else {
            timeObject.title
        }

        if(timeObject.repeat.isNullOrBlank()) {
            v.repeatText.visibility = View.GONE
        }else {
            v.repeatText.visibility = View.VISIBLE
            v.repeatText.text = RepeatManager.makeRepeatText(timeObject)
        }

        if(timeObject.location.isNullOrBlank()) {
            v.locationText.visibility = View.GONE
        }else {
            v.locationText.visibility = View.VISIBLE
            v.locationText.text = timeObject.location
        }

        if(timeObject.description.isNullOrBlank()) {
            v.memoText.visibility = View.GONE
        }else {
            v.memoText.visibility = View.VISIBLE
            v.memoText.text = timeObject.description
        }

        if(timeObject.alarms.any{ it.dtAlarm >= System.currentTimeMillis() }) {
            v.alarmIndi.visibility = View.VISIBLE
        }else {
            v.alarmIndi.visibility = View.GONE
        }

        v.checkBox.setColorFilter(timeObject.color)

        if(timeObject.isDone()) {
            v.checkBox.setImageResource(R.drawable.checked)
            v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else {
            v.checkBox.setImageResource(R.drawable.unchecked)
            v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }

        v.setOnClickListener { adapterInterface.invoke(it, timeObject, 0) }
        v.checkArea.setOnClickListener { adapterInterface.invoke(it, timeObject, 1) }
        v.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener false
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val target = items[fromPosition]
        if(!target.isDone()) {
            val limitIndex = items.indexOf(items.last { !it.isDone() })
            if(toPosition <= limitIndex) {
                Collections.swap(items, fromPosition, toPosition)
                notifyItemMoved(fromPosition, toPosition)
                return true
            }
        }
        return false
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: TaskListAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            reordering = reordering or mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Fade out the view as it is swiped out of the parent's bounds
                val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
                viewHolder.itemView.translationX = dX
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is TaskListAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TaskListAdapter.ViewHolder?
                    itemViewHolder?.onItemSelected()
                }
            }else if(reordering && actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                reordering = false
                TimeObjectManager.reorder(items.filter { !it.isDone() })
            }

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
            (viewHolder as? TaskListAdapter.ViewHolder)?.onItemClear()
        }
    }
}