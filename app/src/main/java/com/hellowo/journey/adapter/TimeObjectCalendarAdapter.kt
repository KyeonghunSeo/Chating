package com.hellowo.journey.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.calendar.CalendarComparator
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.repeat.RepeatManager
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.TimeObjectView
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TimeObjectCalendarAdapter(private var items : RealmResults<TimeObject>, private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<TimeObjectViewHolder>()
    private val viewLevelStatusMap = HashMap<Int, ViewLevelStatus>()
    private val context = calendarView.context
    private val columns = CalendarView.columns
    private var rows = 0
    private var maxCellNum = 0
    private var calStartTime = 0L
    private var withAnimtion = false
    private val drawStartYOffset = CalendarView.dateArea + /*날짜와 블록 마진*/dpToPx(2)
    private val cellBottomArray = Array(42){ _ -> drawStartYOffset}
    private val rowHeightArray = Array(6){ _ -> drawStartYOffset}

    fun draw() {
        setCalendarData()
        makeTimeObjectViewHolder()
        setTimeObjectViews()
        refreshCalendarView()
    }

    fun refresh(result: RealmResults<TimeObject>, anim: Boolean) {
        items = result
        withAnimtion = anim
        viewHolderList.forEach { holder ->
            holder.timeObjectViewList?.forEach { (it.parent as FrameLayout).removeView(it) }
        }
        viewHolderList.clear()
        viewLevelStatusMap.clear()
        cellBottomArray.fill(drawStartYOffset)
        rowHeightArray.fill(drawStartYOffset)
        draw()
    }

    private fun setCalendarData() {
        rows = calendarView.rows
        maxCellNum = rows * columns
        calStartTime = calendarView.calendarStartTime
    }

    private fun makeTimeObjectViewHolder() {
        items.forEach{ timeObject ->
            try{
                if(timeObject.repeat.isNullOrEmpty()) {
                    computPosition(timeObject)
                }else {
                    RepeatManager.makeRepeatInstance(timeObject,
                            calendarView.calendarStartTime, calendarView.calendarEndTime)
                            .forEach { computPosition(it) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
        viewHolderList.sortWith(CalendarComparator())
    }

    private fun computPosition(timeObject: TimeObject) {
        val info = TimeObjectViewHolder(timeObject)
        info.startCellNum = ((timeObject.dtStart - calStartTime) / DAY_MILL).toInt()
        info.endCellNum = ((timeObject.dtEnd - calStartTime) / DAY_MILL).toInt()

        var lOpen = false
        var rOpen = false

        if(info.startCellNum < 0) {
            info.startCellNum = 0
            lOpen = true
        }
        if(info.endCellNum >= maxCellNum) {
            info.endCellNum = maxCellNum - 1
            rOpen = true
        }

        var currentCell = info.startCellNum
        var length = info.endCellNum - info.startCellNum + 1
        var margin = columns - currentCell % columns
        val size = 1 + (info.endCellNum / columns - info.startCellNum / columns)

        info.timeObjectViewList = Array(size) { index ->
            TimeObjectView(context, timeObject, currentCell, if (length <= margin) length else margin).apply {
                currentCell += margin
                length -= margin
                margin = 7
                when(index) {
                    0 -> {
                        leftOpen = lOpen
                        rightOpen = size > 1
                    }
                    size - 2 -> {
                        leftOpen = true
                        rightOpen = true
                    }
                    else -> {
                        leftOpen = size > 1
                        rightOpen = rOpen
                    }
                }
            }
        }
        viewHolderList.add(info)
    }

    private fun setTimeObjectViews() {
        var currentViewLevel = -1
        var currentType = -1
        viewHolderList.forEach {
            try{
                val timeObject = it.timeObject
                val viewLevel = timeObject.getViewLevelPriority()
                val formula = timeObject.getFormula()
                val status = viewLevelStatusMap[viewLevel]?: ViewLevelStatus().apply { viewLevelStatusMap[viewLevel] = this }

                if(currentViewLevel == -1) { currentViewLevel = viewLevel }
                if(currentType == -1) { currentType = it.timeObject.type }

                if(viewLevel != currentViewLevel) {
                    currentViewLevel = viewLevel
                    computeRowHeight()
                }

                if(currentType != it.timeObject.type) {
                    currentType = it.timeObject.type
                    setTypeMargin()
                }

                it.timeObjectViewList?.forEach {
                    it.mLeft = (calendarView.minWidth * (it.cellNum % columns)).toInt() + CalendarView.weekSideMargin
                    it.mRight = it.mLeft + (calendarView.minWidth * it.length).toInt()
                    when(formula) {
                        TimeObject.Formula.TOPSTACK -> {
                            it.mTop = computeOrder(it, status) * it.getViewHeight() /*블럭수에 따른 높이*/ + rowHeightArray[it.cellNum / columns]
                        }
                        TimeObject.Formula.LINEAR -> {
                            //l("viewLevel : ${viewLevel}, ${it.cellNum} : ${cellBottomArray[it.cellNum]}")
                            it.mTop = cellBottomArray[it.cellNum]
                        }
                        else -> {}
                    }
                    it.mBottom = it.mTop + it.getViewHeight()
                    it.setLayout()
                    (it.cellNum until it.cellNum + it.length).forEach{ index ->
                        cellBottomArray[index] = Math.max(cellBottomArray[index], it.mBottom)
                    }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
        computeRowHeight()
    }

    private fun computeRowHeight() {
        (0..5).forEach{ index ->
            rowHeightArray[index] = cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0 + TimeObjectView.levelMargin
        }
    }

    private fun setTypeMargin() {
        val typeMargin = dpToPx(3)
        cellBottomArray.forEachIndexed { index, i -> cellBottomArray[index] += typeMargin }
    }

    private fun refreshCalendarView() {
        if(withAnimtion) {
            TransitionManager.beginDelayedTransition(calendarView.calendarLy, makeChangeBounceTransition())
            withAnimtion = false
        }
        var calendarHeight = 0
        val minHeight = calendarView.minHeight.toInt()
        val bottomPadding = CalendarView.weekLyBottomPadding

        calendarView.weekLys.forEachIndexed { index, weekLy ->
            if(index < rows) {
                val newHeight = rowHeightArray[index] + bottomPadding
                val finalHeight = Math.max(minHeight, newHeight)
                calendarHeight += finalHeight
                weekLy.layoutParams.height = finalHeight
            }
        }

        viewHolderList.forEach {
            try{
                it.timeObjectViewList?.forEach {
                    //it.alpha = if(it.cellNum + it.length - 1 in calendarView.startCellNum..calendarView.endCellNum) 1f else 0.2f
                    calendarView.dateCells[it.cellNum].addView(it)
                    if(TimeObjectManager.lastUpdatedItem == it.timeObject) {
                        showInsertAnimation(it)
                    }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }

        calendarView.calendarLy.layoutParams.height = calendarHeight
        calendarView.calendarLy.requestLayout()
    }

    private fun showInsertAnimation(view: TimeObjectView) {
        TimeObjectManager.lastUpdatedItem = null
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f).setDuration(500),
                ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f).setDuration(500))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    private fun computeOrder(view: TimeObjectView, status: ViewLevelStatus): Int {
        var order = 0
        for (i in view.cellNum until view.cellNum + view.length) {
            val s = StringBuilder(status.status[i])
            if(i == view.cellNum) {
                order = s.indexOf("0") // 빈공간 찾기
                if(order == -1) order = s.length // 빈공간이 없으면 가장 마지막 순서
            }

            if(order >= s.length) {
                s.append(CharArray(order - s.length + 1) { _-> '0'}) // 빈공간 채우기
            }
            status.status[i] = s.replaceRange(order, order + 1, "1").toString() // 빈공간을 채우고 상태 갱신
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

    inner class ViewLevelStatus {
        var status = Array(maxCellNum){ _ -> "0" }
    }
}