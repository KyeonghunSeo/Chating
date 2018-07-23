package com.hellowo.chating.calendar

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TimeObjectAdapter(private var items : RealmResults<TimeObject>, private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<TimeObjectViewHolder>()
    private val viewPositionStatusMap = HashMap<Int, ViewPositionStatus>()
    private val context = calendarView.context
    private val columns = CalendarView.columns
    private var rows = 0
    private var maxCellNum = 0
    private var calStartTime = 0L
    private var withAnimtion = false
    private val cellBottomArray = Array(42){ _ -> 0}
    private val rowHeightArray = Array(6){ _ -> 0}

    fun draw() {
        setCalendarData()
        computePosition()
        setTimeObjectViews()
        refreshCalendarView()
    }

    fun refresh(result: RealmResults<TimeObject>) {
        items = result
        withAnimtion = true
        viewHolderList.clear()
        viewPositionStatusMap.clear()
        cellBottomArray.fill(0)
        rowHeightArray.fill(0)
        draw()
    }

    private fun setCalendarData() {
        rows = calendarView.rows
        maxCellNum = rows * columns
        calStartTime = calendarView.calendarStartTime
    }

    private fun computePosition() {
        items.forEach{
            try{
                if(!viewPositionStatusMap.containsKey(it.type)) {
                    viewPositionStatusMap[it.type] = ViewPositionStatus()
                }

                val info = TimeObjectViewHolder(it)
                info.startCellNum = ((it.dtStart - calStartTime) / DAY_MILL).toInt()
                info.endCellNum = ((it.dtEnd - calStartTime) / DAY_MILL).toInt()

                if(info.startCellNum < 0) info.startCellNum = 0
                if(info.endCellNum >= maxCellNum) info.endCellNum = maxCellNum - 1

                var currentCell = info.startCellNum
                var length = info.endCellNum - info.startCellNum + 1
                var margin = columns - currentCell % columns

                info.timeObjectViewList = Array(1 + (info.endCellNum / columns - info.startCellNum / columns)) { _ ->
                    TimeObjectView(context, it, currentCell, if(length <= margin) length else margin).apply {
                        currentCell += margin
                        length -= margin
                        margin = 7
                    }
                }
                viewHolderList.add(info)
            }catch (e: Exception){ e.printStackTrace() }
        }
        viewHolderList.sortWith(CalendarComparator())
    }

    private fun setTimeObjectViews() {
        val levelMargin = dpToPx(3)
        var startTopPos = 0
        var currentType = 0
        viewHolderList.forEach {
            try{
                val status = viewPositionStatusMap[it.timeObject.type]!!
                if(it.timeObject.type != currentType) {
                    currentType = it.timeObject.type
                    (0..5).forEach{ index ->
                        rowHeightArray[index] = cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0 + levelMargin
                    }
                }
                it.timeObjectViewList?.forEach {
                    it.mOrder = computeOrder(it, status)
                    it.mLeft = (calendarView.minWidth * (it.cellNum % columns)).toInt()
                    it.mRight = it.mLeft + (calendarView.minWidth * it.Length).toInt()
                    it.mTop = calendarView.dateArea + it.mOrder * it.getTypeHeight() + it.mOrder + rowHeightArray[it.cellNum / columns]
                    it.mBottom = it.mTop + it.getTypeHeight()
                    it.setLayout()
                    cellBottomArray[it.cellNum] = Math.max(cellBottomArray[it.cellNum], it.mBottom)
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun refreshCalendarView() {
        if(withAnimtion) {
            TransitionManager.beginDelayedTransition(calendarView.calendarLy, makeChangeBounceTransition())
            withAnimtion = false
        }
        var calendarHeight = 0
        val minHeight = calendarView.minHeight.toInt()
        val bottomPadding = calendarView.weekLyBottomPadding
        calendarView.weekLys.forEachIndexed { index, ly ->
            if(ly.childCount > columns) {
                ly.removeViews(columns, ly.childCount - columns)
            }
            if(index < rows) {
                val newHeight = cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0 + bottomPadding
                val finalHeight = Math.max(minHeight, newHeight)
                calendarHeight += finalHeight
                ly.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, finalHeight)
            }
        }
        calendarView.calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, calendarHeight)
        viewHolderList.forEach {
            try{
                it.timeObjectViewList?.forEach {
                    calendarView.weekLys[it.cellNum / columns].addView(it)
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun computeOrder(view: TimeObjectView, status: ViewPositionStatus): Int {
        var order = 0
        for (i in view.cellNum until view.cellNum + view.Length) {
            val s = StringBuilder(status.status[i])
            if(i == view.cellNum) {
                order = s.indexOf("0")
                if(order == -1) order = s.length
            }

            if(order < s.length) {
                status.status[i] = s.replace(order, order, "1").toString()
            }else {
                if(order == s.length) {
                    status.status[i] = s.append(s.length, "1").toString()
                }else {
                    s.append("0", s.length, order - 1)
                    status.status[i] = s.append(s.length, "1").toString()
                }
            }
        }
        return order
    }

    fun getViews(cellNum: Int) : List<TimeObjectView> {
        val result = ArrayList<TimeObjectView>()
        viewHolderList.filter { it.startCellNum == cellNum }.forEach {
            it.timeObjectViewList?.let { result.addAll(it) }
        }
        return result
    }

    inner class TimeObjectViewHolder(val timeObject: TimeObject) {
        var startCellNum = 0
        var endCellNum = 0
        var timeObjectViewList : Array<TimeObjectView>? = null
        override fun toString(): String {
            return "TimeObjectViewHolder(timeObject=$timeObject, startCellNum=$startCellNum, endCellNum=$endCellNum, timeObjectViewList=${Arrays.toString(timeObjectViewList)})"
        }
    }

    inner class ViewPositionStatus {
        var status = Array(maxCellNum){ _ -> "0" }
    }
}