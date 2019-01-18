package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.setGlobalTheme
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
            setGlobalTheme(container)
            container.backLy.setBackgroundColor(AppTheme.backgroundDarkColor)
            container.frontLy.setBackgroundColor(AppTheme.backgroundColor)
        }

        fun onItemSelected() {
            //itemView.setBackgroundColor(AppDateFormat.almostWhite)
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

        if(timeObject.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
        }else {
            v.tagText.visibility = View.GONE
        }

        val title = StringBuilder()
        if(timeObject.title.isNullOrBlank()) {
            title.append(context.getString(R.string.untitle))
        }else {
            title.append(timeObject.title?.trim())
        }

        if(!timeObject.repeat.isNullOrBlank()) {
            title.append(" (${ RepeatManager.makeRepeatText(timeObject)})")
        }
        v.titleText.text = title

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

        v.checkBox.setColorFilter(timeObject.getColor())

        if(timeObject.isDone()) {
            v.checkBox.setImageResource(R.drawable.sharp_check_box_black_48dp)
            v.checkBox.alpha = 0.3f
            v.contentLy.alpha = 0.3f
            v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else {
            v.checkBox.setImageResource(R.drawable.sharp_check_box_outline_blank_black_48dp)
            v.checkBox.alpha = 1f
            v.contentLy.alpha = 1f
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
        /* 완료된거 안움직이는 로직
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
        */
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
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
                viewHolder.itemView.frontLy.translationX = dX
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
                //TimeObjectManager.reorder(items.filter { !it.isDone() })
                TimeObjectManager.reorder(items)
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