package com.hellowo.chating.calendar

import androidx.transition.TransitionManager
import com.hellowo.chating.DAY_MILL
import com.hellowo.chating.l
import com.hellowo.chating.makeFadeInTransition
import com.hellowo.chating.makeFromBottomSlideTransition
import io.realm.RealmResults
import java.util.*
import kotlin.collections.HashMap


class TimeObjectAdapter(private val items : RealmResults<TimeObject>, private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<TimeObjectViewHolder>()
    private val viewPositionStatusMap = HashMap<Int, ViewPositionStatus>()
    private val context = calendarView.context
    private val rows = calendarView.rows
    private val columns = CalendarView.columns
    private val maxCellNum = rows * columns
    private val calStartTime = calendarView.calendarStartTime

    fun draw() {
        computePosition()
        setTimeObjectViews()
        refreshCalendarView()
    }

    private fun computePosition() {
        items.forEach {
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
                var length = info.startCellNum - info.startCellNum + 1
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
        viewHolderList.sortWith(TimeObjectCalendarComparator())
    }

    private fun setTimeObjectViews() {
        viewHolderList.forEach {
            try{
                val status = viewPositionStatusMap[it.timeObject.type]!!
                it.timeObjectViewList?.forEach {
                    it.mOrder = computeOrder(it, status)
                    it.mLeft = (calendarView.cellW * (it.cellNum % columns)).toInt()
                    it.mRight = it.mLeft + (calendarView.cellW * it.Length).toInt() - 1 /* 마진 */
                    it.mTop = calendarView.dateArea + it.mOrder * it.getTypeHeight() + it.mOrder
                    it.mBottom = it.mTop + it.getTypeHeight()
                    it.setLayout()
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun refreshCalendarView() {
        calendarView.weekLys.forEach {
            //TransitionManager.beginDelayedTransition(it, makeFromBottomSlideTransition())
            if(it.childCount > columns) {
                it.removeViews(columns, it.childCount - columns)
            }
        }
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
        if(order > status.maxOrder) status.maxOrder = order
        return order
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
        var maxOrder = 0
        var status = Array(maxCellNum){ _ -> "0" }
    }
}