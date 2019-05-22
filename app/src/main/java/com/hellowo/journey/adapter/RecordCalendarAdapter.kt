package com.hellowo.journey.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.FrameLayout
import androidx.transition.TransitionManager
import com.hellowo.journey.AppStatus
import com.hellowo.journey.DAY_MILL
import com.hellowo.journey.adapter.util.RecordCalendarComparator
import com.hellowo.journey.dpToPx
import com.hellowo.journey.makeChangeBounceTransition
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.model.Record
import com.hellowo.journey.model.Record.Formula.*
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.CalendarView.Companion.weekLyBottomPadding
import com.hellowo.journey.ui.view.RecordView
import com.hellowo.journey.ui.view.RecordView.Companion.blockTypeSize
import io.realm.RealmResults


class RecordCalendarAdapter(private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<TimeObjectViewHolder>()
    private val viewLevelStatusMap = HashMap<Record.Formula, ViewLevelStatus>()
    private val context = calendarView.context
    private val columns = CalendarView.columns
    private var rows = 0
    private var maxCellNum = 0
    private var calStartTime = 0L
    private var withAnimtion = false
    private var minWidth = 0f
    private var minHeight = 0f
    private var drawStartYOffset = 0f
    private val cellBottomArray = Array(42){ drawStartYOffset}
    private val rowHeightArray = Array(6){ drawStartYOffset}

    fun draw(items : RealmResults<Record>?) {
        setCalendarData()
        makeTimeObjectViewHolder(items)
        calculateTimeObjectViewsPosition()
        drawTimeObjectViewOnCalendarView()
    }

    fun refresh(items: RealmResults<Record>?, anim: Boolean) {
        withAnimtion = anim
        viewHolderList.forEach { holder ->
            holder.timeObjectViewList.forEach { (it.parent as FrameLayout).removeView(it) }
        }
        viewHolderList.clear()
        viewLevelStatusMap.clear()
        drawStartYOffset = if(false) dpToPx(22f) else dpToPx(33f)
        cellBottomArray.fill(drawStartYOffset)
        rowHeightArray.fill(drawStartYOffset)
        draw(items)
    }

    private fun setCalendarData() {
        rows = calendarView.rows
        minWidth = calendarView.minWidth
        minHeight = calendarView.minHeight
        maxCellNum = rows * columns
        calStartTime = calendarView.calendarStartTime
    }

    private fun makeTimeObjectViewHolder(items : RealmResults<Record>?) {
        items?.forEach{ timeObject ->
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
        viewHolderList.sortWith(RecordCalendarComparator())
    }

    private fun makeTimeObjectView(record: Record) {
        var startCellNum = ((record.dtStart - calStartTime) / DAY_MILL).toInt()
        var endCellNum = ((record.dtEnd - calStartTime) / DAY_MILL).toInt()
        when(record.getFormula()){
            TOP_FLOW -> {
                val holder = viewHolderList
                        .firstOrNull { it.record.type == record.type && it.startCellNum == startCellNum }
                        ?: TimeObjectViewHolder(record, startCellNum, endCellNum).apply { viewHolderList.add(this) }
                val timeObjectView = holder.timeObjectViewList.firstOrNull() ?:
                RecordView(context, record, startCellNum, 1).apply {
                    childList = ArrayList()
                    setLookByType()
                    holder.timeObjectViewList.add(this) }

                timeObjectView.childList?.add(record)
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

                val holder = TimeObjectViewHolder(record, startCellNum, endCellNum)
                var currentCell = holder.startCellNum
                var length = holder.endCellNum - holder.startCellNum + 1
                var margin = columns - currentCell % columns
                val size = 1 + (holder.endCellNum / columns - holder.startCellNum / columns)

                (0 until size).forEach { index ->
                    holder.timeObjectViewList.add(
                            RecordView(context, record, currentCell,
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
    }

    private fun calculateTimeObjectViewsPosition() {
        var currentFomula = Record.Formula.BACKGROUND
        viewHolderList.forEach { viewHolder ->
            try{
                val timeObject = viewHolder.record
                val formula = timeObject.getFormula()
                val status
                        = viewLevelStatusMap[formula]?: ViewLevelStatus().apply { viewLevelStatusMap[formula] = this }

                if(formula != currentFomula) {
                    currentFomula = formula
                    when(currentFomula) {
                        SINGLE_LINE_BOTTOM_STACK -> {
                            addBottomMargin(20f)
                            computeBottomStackStartPos()
                        }
                        OVERLAY -> {
                            computeMaxRowHeight()
                        }
                        else -> {}
                    }
                }

                viewHolder.timeObjectViewList.forEach {
                    it.mLeft = (minWidth * (it.cellNum % columns)) + CalendarView.calendarPadding
                    it.mRight = it.mLeft + (minWidth * it.length).toInt()
                    if(true) {
                        when(formula) {
                            SINGLE_LINE_TOP_STACK -> {
                                it.mTop = computeOrder(it, status) * it.getViewHeight() + rowHeightArray[it.cellNum / columns]
                                it.mBottom = it.mTop + it.getViewHeight()
                            }
                            TOP_FLOW, MULTI_LINE_TOP -> {
                                it.mTop = cellBottomArray[it.cellNum]
                                it.mBottom = it.mTop + it.getViewHeight()
                            }
                            BOTTOM_LINEAR -> {
                                it.mTop = cellBottomArray[it.cellNum]
                                it.mBottom = it.mTop + it.getViewHeight()
                            }
                            SINGLE_LINE_BOTTOM_STACK -> {
                                it.mTop = computeOrder(it, status) * it.getViewHeight() + rowHeightArray[it.cellNum / columns]
                                it.mBottom = it.mTop + it.getViewHeight()
                            }
                            OVERLAY -> {
                                it.mTop = drawStartYOffset
                                it.mBottom = minHeight
                            }
                            else -> {}
                        }
                        (it.cellNum until it.cellNum + it.length).forEach{ index ->
                            cellBottomArray[index] = Math.max(cellBottomArray[index], it.mBottom)
                        }
                    }else { // 바텀 패딩쪽에 그리고 싶을때
                        it.mTop = Math.max(minHeight - weekLyBottomPadding,
                                rowHeightArray[it.cellNum / columns])
                        it.mBottom = it.mTop + weekLyBottomPadding
                    }
                    it.setLayout()
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

    private fun computeBottomStackStartPos() {
        (0..5).forEach{ index ->
            rowHeightArray[index] = Math.max(minHeight - blockTypeSize,
                    cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0f)
        }
    }

    private fun addBottomMargin(margin: Float) {
        cellBottomArray.forEachIndexed { index, i -> cellBottomArray[index] += margin }
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
            var lastAlpha = if(holder.record.isDone()) 1f else 1f
            if(calendarView.lastUpdatedItem?.isValid == true && calendarView.lastUpdatedItem?.id == holder.record.id) { // 마지막 업데이트 오브젝트
                calendarView.lastUpdatedItem = null
                holder.timeObjectViewList.forEach {
                    lastAlpha = if(isDateInMonth(it)) lastAlpha else AppStatus.outsideMonthAlpha
                    it.alpha = 0f
                    calendarView.dateCells[it.cellNum].addView(it)
                    it.post { showInsertAnimation(it, lastAlpha) }
                }
            }else {
                holder.timeObjectViewList.forEach {
                    it.alpha = if(isDateInMonth(it)) lastAlpha else AppStatus.outsideMonthAlpha
                    calendarView.dateCells[it.cellNum].addView(it)
                }
            }
        }

        calendarView.calendarLy.requestLayout()
    }

    private fun isDateInMonth(view: RecordView) = view.cellNum in calendarView.startCellNum..calendarView.endCellNum
            || view.cellNum + view.length - 1 in calendarView.startCellNum..calendarView.endCellNum

    private fun showInsertAnimation(view: RecordView, lastAlpha: Float) {
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", blockTypeSize.toFloat(), 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, lastAlpha))
        animSet.duration = 1000
        animSet.interpolator = AnticipateOvershootInterpolator()
        animSet.start()
    }

    private fun computeOrder(view: RecordView, status: ViewLevelStatus): Int {
        var order = 0
        for (i in view.cellNum until view.cellNum + view.length) {
            val s = StringBuilder(status.status[i])

            if(i == view.cellNum) {
                var findPosition = false
                order = s.indexOf("0") // 빈공간 찾기
                if(order == -1) order = s.length // 빈공간이 없으면 가장 마지막 순서

                if(view.length > 1) {
                    while(!findPosition) {
                        var breakPoint = false
                        for (j in view.cellNum until view.cellNum + view.length) {
                            if(order < status.status[j].length && status.status[j][order] == '1') {
                                breakPoint = true
                                break
                            }
                        }
                        if(breakPoint) {
                            order++
                        }else {
                            findPosition = true
                        }
                    }
                }
            }

            if(order >= s.length) {
                s.append(CharArray(order - s.length + 1) {'0'}) // 빈공간 채우기
            }
            status.status[i] = s.replaceRange(order, order + 1, "1").toString() // 빈공간을 채우고 상태 갱신
        }
        return order
    }

    fun getViews(cellNum: Int) : List<RecordView> {
        val result = ArrayList<RecordView>()
        viewHolderList.filter { it.startCellNum == cellNum }.forEach { viewHolder ->
            viewHolder.timeObjectViewList.let { result.addAll(it) }
        }
        return result
    }

    inner class TimeObjectViewHolder(val record: Record, val startCellNum: Int, val endCellNum: Int) {
        val timeObjectViewList = ArrayList<RecordView>()
    }

    inner class ViewLevelStatus {
        var status = Array(maxCellNum){ "0" }
    }
}