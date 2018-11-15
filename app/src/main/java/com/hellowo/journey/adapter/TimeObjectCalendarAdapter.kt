package com.hellowo.journey.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.util.CalendarComparator
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.CalendarView.Companion.weekLyBottomPadding
import com.hellowo.journey.ui.view.TimeObjectView
import com.hellowo.journey.ui.view.TimeObjectView.Companion.normalTypeSize
import io.realm.RealmResults
import com.hellowo.journey.model.TimeObject.Type.*
import com.hellowo.journey.model.TimeObject.Formula.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TimeObjectCalendarAdapter(private var items : RealmResults<TimeObject>, private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<TimeObjectViewHolder>()
    private val viewLevelStatusMap = HashMap<TimeObject.Formula, ViewLevelStatus>()
    private val context = calendarView.context
    private val columns = CalendarView.columns
    private var rows = 0
    private var maxCellNum = 0
    private var calStartTime = 0L
    private var withAnimtion = false
    private var minWidth = 0f
    private var minHeight = 0f
    private val drawStartYOffset = CalendarView.dateArea + /*날짜와 블록 마진*/dpToPx(2f)
    private val cellBottomArray = Array(42){ _ -> drawStartYOffset}
    private val rowHeightArray = Array(6){ _ -> drawStartYOffset}
    private val notInCalendarList = Array(42){ _ -> ArrayList<TimeObject>()}

    fun draw() {
        setCalendarData()
        makeTimeObjectViewHolder()
        calculateTimeObjectViewsPosition()
        drawTimeObjectViewOnCalendarView()
    }

    fun refresh(result: RealmResults<TimeObject>, anim: Boolean) {
        items = result
        withAnimtion = anim
        viewHolderList.forEach { holder ->
            holder.timeObjectViewList.forEach { (it.parent as FrameLayout).removeView(it) }
        }
        viewHolderList.clear()
        viewLevelStatusMap.clear()
        cellBottomArray.fill(drawStartYOffset)
        rowHeightArray.fill(drawStartYOffset)
        notInCalendarList.forEach { it.clear() }
        draw()
    }

    private fun setCalendarData() {
        rows = calendarView.rows
        minWidth = calendarView.minWidth
        minHeight = calendarView.minHeight
        maxCellNum = rows * columns
        calStartTime = calendarView.calendarStartTime
    }

    private fun makeTimeObjectViewHolder() {
        items.forEach{ timeObject ->
            try{
                if(timeObject.repeat.isNullOrEmpty()) {
                    makeTimeObjectView(timeObject)
                }else {
                    RepeatManager.makeRepeatInstance(timeObject,
                            calendarView.calendarStartTime, calendarView.calendarEndTime)
                            .forEach { makeTimeObjectView(it) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }

        OsCalendarManager.getInstances(context, "", calendarView.calendarStartTime, calendarView.calendarEndTime).forEach {
            makeTimeObjectView(it)
        }
        viewHolderList.sortWith(CalendarComparator())
    }

    private fun makeTimeObjectView(timeObject: TimeObject) {
        var startCellNum = ((timeObject.dtStart - calStartTime) / DAY_MILL).toInt()
        var endCellNum = ((timeObject.dtEnd - calStartTime) / DAY_MILL).toInt()
        if(timeObject.inCalendar) {
            when(TimeObject.Type.values()[timeObject.type]){
                TimeObject.Type.STAMP, TimeObject.Type.MONEY -> {
                    val holder = viewHolderList
                            .firstOrNull { it.timeObject.type == timeObject.type && it.startCellNum == startCellNum }
                            ?: TimeObjectViewHolder(timeObject, startCellNum, endCellNum).apply { viewHolderList.add(this) }

                    val timeObjectView = holder.timeObjectViewList.firstOrNull() ?:
                        TimeObjectView(context, timeObject, startCellNum, 1).apply {
                            childList = ArrayList()
                            setLookByType()
                            holder.timeObjectViewList.add(this) }

                    timeObjectView.childList?.add(timeObject)
                }
                else -> {
                    var lOpen = false
                    var rOpen = false

                    if(startCellNum < 0) {
                        startCellNum = 0
                        lOpen = true
                    }
                    if(endCellNum >= maxCellNum) {
                        endCellNum = maxCellNum - 1
                        rOpen = true
                    }

                    val holder = TimeObjectViewHolder(timeObject, startCellNum, endCellNum)
                    var currentCell = holder.startCellNum
                    var length = holder.endCellNum - holder.startCellNum + 1
                    var margin = columns - currentCell % columns
                    val size = 1 + (holder.endCellNum / columns - holder.startCellNum / columns)

                    (0 until size).forEach { index ->
                        holder.timeObjectViewList.add(
                                TimeObjectView(context, timeObject, currentCell,
                                        if (length <= margin) length else margin).apply {
                                    currentCell += margin
                                    length -= margin
                                    margin = 7
                                    when(holder.timeObjectViewList.size) {
                                        0 -> {
                                            leftOpen = lOpen
                                            rightOpen = size > 1
                                        }
                                        size - 1 -> {
                                            leftOpen = size > 1
                                            rightOpen = rOpen
                                        }
                                        else -> {
                                            leftOpen = true
                                            rightOpen = true
                                        }
                                    }
                                    setLookByType()
                                })}
                    viewHolderList.add(holder)
                }
            }
        }else {
            (startCellNum .. endCellNum).forEach { notInCalendarList[it].add(timeObject) }
        }
    }

    private fun calculateTimeObjectViewsPosition() {
        var currentFomula = TimeObject.Formula.BACKGROUND
        var currentType = TimeObject.Type.EVENT
        viewHolderList.forEach { viewHolder ->
            try{
                val timeObject = viewHolder.timeObject
                val formula = timeObject.getFormula()
                val status = viewLevelStatusMap[formula]?: ViewLevelStatus().apply { viewLevelStatusMap[formula] = this }

                if(currentType.ordinal != timeObject.type) {
                    currentType = TimeObject.Type.values()[timeObject.type]
                    when(currentType) {
                        TASK -> setTypeMargin(dpToPx(4f), currentType)
                        STAMP, MONEY -> setTypeMargin(dpToPx(3f), currentType)
                        NOTE -> setTypeMargin(dpToPx(4F), currentType)
                        TERM -> addBottomMargin(dpToPx(18f))
                        else -> {}
                    }
                }

                if(formula != currentFomula) {
                    currentFomula = formula
                    when(currentFomula) {
                        BOTTOM_LINEAR -> computeBottomLinearStartPos()
                        BOTTOM_STACK -> {
                            computeMaxRowHeight()
                            computeBottomStackStartPos()
                        }
                    }
                }

                viewHolder.timeObjectViewList.forEach {
                    it.mLeft = (minWidth * (it.cellNum % columns)) + CalendarView.weekSideMargin
                    it.mRight = it.mLeft + (minWidth * it.length).toInt()
                    when(formula) {
                        TOP_STACK -> {
                            it.mTop = computeOrder(it, status) * it.getViewHeight() + rowHeightArray[it.cellNum / columns]
                            it.mBottom = it.mTop + it.getViewHeight()
                        }
                        TOP_FLOW, TOP_LINEAR, MID_FLOW -> {
                            it.mTop = cellBottomArray[it.cellNum]
                            it.mBottom = it.mTop + it.getViewHeight()
                        }
                        BOTTOM_LINEAR -> {
                            it.mTop = cellBottomArray[it.cellNum]
                            it.mBottom = it.mTop + it.getViewHeight()
                        }
                        BOTTOM_STACK -> {
                            it.mTop = computeOrder(it, status) * it.getViewHeight() + rowHeightArray[it.cellNum / columns]
                            it.mBottom = it.mTop + it.getViewHeight()
                        }
                        OVERLAY -> {
                            it.mTop = drawStartYOffset
                            it.mBottom = calendarView.minHeight
                        }
                    }
                    it.setLayout()

                    (it.cellNum until it.cellNum + it.length).forEach{ index ->
                        cellBottomArray[index] = Math.max(cellBottomArray[index], it.mBottom) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
        computeMaxRowHeight()
    }

    private fun computeMaxRowHeight() { // 1주일중 가장 높이가 높은곳 계산
        (0..5).forEach{ index ->
            rowHeightArray[index] = cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0f
        }
    }

    private fun computeBottomLinearStartPos() {
        cellBottomArray.forEachIndexed { index, i ->
            cellBottomArray[index] = Math.max(minHeight - weekLyBottomPadding - normalTypeSize, cellBottomArray[index])
        }
    }

    private fun computeBottomStackStartPos() {
        (0..5).forEach{ index ->
            rowHeightArray[index] = Math.max(minHeight - weekLyBottomPadding - normalTypeSize, rowHeightArray[index])
        }
    }

    private fun addBottomMargin(margin: Float) {
        cellBottomArray.forEachIndexed { index, i -> cellBottomArray[index] += margin }
    }

    private fun setTypeMargin(margin: Float, type: TimeObject.Type) {
        viewHolderList.filter { it.timeObject.type == type.ordinal }.distinctBy { it.startCellNum }.forEach {
            if(cellBottomArray[it.startCellNum] > drawStartYOffset) cellBottomArray[it.startCellNum] += margin
        }
    }


    private fun drawTimeObjectViewOnCalendarView() {
        if(withAnimtion) {
            TransitionManager.beginDelayedTransition(calendarView.calendarLy, makeChangeBounceTransition())
            withAnimtion = false
        }
        var calendarHeight = 0f

        calendarView.weekLys.forEachIndexed { index, weekLy ->
            if(index < rows) {
                val newHeight = rowHeightArray[index] + weekLyBottomPadding
                val finalHeight = Math.max(minHeight, newHeight)
                calendarHeight += finalHeight
                weekLy.layoutParams.height = finalHeight.toInt()
            }
        }

        viewHolderList.forEach { holder ->
            if(TimeObjectManager.lastUpdatedItem?.isValid == true
                    && TimeObjectManager.lastUpdatedItem?.id == holder.timeObject.id) {
                TimeObjectManager.lastUpdatedItem = null
                holder.timeObjectViewList.forEach {
                    //val lastAlpha = if(isOutDate(it)) 1f else CalendarView.outDateAlpha
                    it.alpha = 0f
                    calendarView.dateCells[it.cellNum].addView(it)
                    it.post { showInsertAnimation(it, 1f) }
                }
            }else {
                holder.timeObjectViewList.forEach {
                    //it.alpha = if(isOutDate(it)) 1f else CalendarView.outDateAlpha
                    calendarView.dateCells[it.cellNum].addView(it)
                }
            }
        }

        calendarView.calendarLy.requestLayout()
    }

    private fun isOutDate(view: TimeObjectView) = view.cellNum in calendarView.startCellNum..calendarView.endCellNum ||
            view.cellNum + view.length - 1 in calendarView.startCellNum..calendarView.endCellNum

    private fun showInsertAnimation(view: TimeObjectView, lastAlpha: Float) {
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", TimeObjectView.normalTypeSize.toFloat(), 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, lastAlpha))
        animSet.duration = 500
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
        viewHolderList.filter { it.startCellNum == cellNum }.forEach { viewHolder ->
            viewHolder.timeObjectViewList.let { result.addAll(it) }
        }
        return result
    }

    inner class TimeObjectViewHolder(val timeObject: TimeObject, val startCellNum: Int, val endCellNum: Int) {
        val timeObjectViewList = ArrayList<TimeObjectView>()
    }

    inner class ViewLevelStatus {
        var status = Array(maxCellNum){ _ -> "0" }
    }
}