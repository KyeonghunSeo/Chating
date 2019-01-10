package com.hellowo.journey.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.ItemTouchHelper
import com.bumptech.glide.Glide
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.manager.RepeatManager
import kotlinx.android.synthetic.main.list_item_event.view.*
import java.util.*

class EventListAdapter(val context: Context, val items: List<TimeObject>, val currentCal: Calendar,
                       val adapterInterface: (view: View, timeObject: TimeObject, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dotSize = dpToPx(6)
    val dotTopMargin = dpToPx(19)
    val tempCal = Calendar.getInstance()
    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_event, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.frontLy.setBackgroundColor(CalendarManager.backgroundColor)

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

        if(timeObject.links.isNotEmpty()){
            Glide.with(context).load(timeObject.links[0]?.data).into(v.imageView)
        }

        v.dotImg.setColorFilter(timeObject.getColor())

        if(timeObject.allday || timeObject.dtStart < getCalendarTime0(currentCal)) {
            tempCal.timeInMillis = timeObject.dtStart
            val totalDate = getDiffDate(timeObject.dtStart, timeObject.dtEnd) + 1
            val toDateNum = getDiffDate(tempCal, currentCal)
            if(totalDate > 1) {
                v.timeText.visibility = View.VISIBLE
                v.timeText.text = String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")
            }else {
                v.timeText.visibility = View.GONE
            }
        }else {
            v.timeText.visibility = View.VISIBLE
            v.timeText.text = "${AppDateFormat.time.format(Date(timeObject.dtStart))} ~ " +
                    "${AppDateFormat.time.format(Date(timeObject.dtEnd))}"
        }

        if(position == 0) {
            v.upperTimeLine.visibility = View.GONE
        }else {
            v.upperTimeLine.visibility = View.VISIBLE
        }

        if(position == items.size - 1) {
            v.bottomTimeLine.visibility = View.GONE
        }else {
            v.bottomTimeLine.visibility = View.VISIBLE
        }

        v.frontLy.setOnClickListener { adapterInterface.invoke(it, timeObject, 0) }
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: EventListAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f

        override fun isLongPressDragEnabled(): Boolean = false

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Fade out the view as it is swiped out of the parent's bounds
                //val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                //viewHolder.itemView.alpha = alpha
                viewHolder.itemView.frontLy.translationX = dX
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is NoteListAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as NoteListAdapter.ViewHolder?
                    itemViewHolder!!.onItemSelected()
                }
            }

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
            (viewHolder as? NoteListAdapter.ViewHolder)?.onItemClear()
        }
    }
}