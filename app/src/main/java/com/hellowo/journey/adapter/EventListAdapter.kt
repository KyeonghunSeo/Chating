package com.hellowo.journey.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.adapter.viewholder.TimeObjectViewHolder
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.list_item_event.view.*
import java.util.*
import android.view.MotionEvent
import com.hellowo.journey.R.id.recyclerView



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

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = TimeObjectViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_event, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.frontLy.setBackgroundColor(CalendarManager.backgroundColor)
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
                    AppDateFormat.time.format(Date(timeObject.dtEnd))
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
        v.deleteBtn.setOnClickListener {
            l("sssssssssssss")
            adapterInterface.invoke(it, timeObject, -1) }
        (holder as TimeObjectViewHolder).setContents(context, timeObject, v, adapterInterface)
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: EventListAdapter) : ItemTouchHelper.Callback() {
        private var buttonShowedState = 0
        private var swipeBack = false
        private val ALPHA_FULL = 1.0f
        private val buttonWidth = dpToPx(50)

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
            if (actionState == ACTION_STATE_SWIPE) {
                l("????????aaaa")
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setTouchListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            recyclerView.setOnTouchListener { v, event ->
                swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                if (swipeBack) {
                    if (dX < -buttonWidth) buttonShowedState = 2
                    else if (dX > buttonWidth) buttonShowedState  = 1

                    if (buttonShowedState != 0) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        setItemsClickable(recyclerView, false)
                    }
                }
                return@setOnTouchListener false
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setTouchDownListener(c: Canvas,
                                         recyclerView: RecyclerView,
                                         viewHolder: RecyclerView.ViewHolder,
                                         dX: Float, dY: Float,
                                         actionState: Int, isCurrentlyActive: Boolean) {
            recyclerView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
                false
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setTouchUpListener(c: Canvas,
                                       recyclerView: RecyclerView,
                                       viewHolder: RecyclerView.ViewHolder,
                                       dX: Float, dY: Float,
                                       actionState: Int, isCurrentlyActive: Boolean) {
            recyclerView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                    recyclerView.setOnTouchListener { v, event -> false }
                    setItemsClickable(recyclerView, true)
                    swipeBack = false
                    buttonShowedState = 0
                }
                false
            }
        }

        private fun setItemsClickable(recyclerView: RecyclerView,
                                      isClickable: Boolean) {
            for (i in 0 until recyclerView.childCount) {
                recyclerView.getChildAt(i).isClickable = isClickable
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is TimeObjectViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TimeObjectViewHolder?
                    itemViewHolder?.onItemSelected()
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
            (viewHolder as? TimeObjectViewHolder)?.onItemClear()
        }

        override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
            if (swipeBack) {
                swipeBack = false
                return 0
            }
            return super.convertToAbsoluteDirection(flags, layoutDirection)
        }
    }
}