package com.ayaan.twelvepages.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.FrameLayout
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.util.RecordCalendarComparator
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import com.ayaan.twelvepages.ui.view.RecordView.Companion.blockTypeSize
import io.realm.RealmResults
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*
import com.ayaan.twelvepages.ui.view.RecordView.Shape.*

class RecordCalendarAdapter(private val calendarView: CalendarView) {
    private val viewHolderList = ArrayList<RecordViewHolder>()
    private val viewLevelStatusMap = HashMap<Formula, ViewLevelStatus>()
    private val context = calendarView.context
    private val columns = CalendarView.columns
    private var rows = 0
    private var maxCellNum = 0
    private var calStartTime = 0L
    private var withAnimtion = false
    private var minWidth = 0f
    private var minHeight = 0f
    private val rowBottomMargin = dpToPx(5f)
    private var drawStartYOffset = CalendarView.dataStartYOffset
    private val cellBottomArray = Array(42){ drawStartYOffset }
    private val rowHeightArray = Array(6){ drawStartYOffset }

    enum class Formula(val nameId: Int, val shapes: Array<RecordView.Shape>) {
        BACKGROUND(R.string.formula_background, arrayOf(BLANK)),
        SINGLE_TEXT(R.string.formula_single_text,
                arrayOf(COLOR_PEN, RECT_FILL_BLUR, RECT_FILL, RECT_STROKE, ROUND_FILL, ROUND_STROKE,
                        THIN_HATCHED, BOLD_HATCHED, NEON_PEN, UNDER_LINE, UPPER_LINE, RANGE,
                        DASH_RANGE, ARROW, DASH_ARROW)),
        MULTI_TEXT(R.string.formula_multi_line_text, arrayOf(COLOR_PEN, RECT_FILL_BLUR, RECT_FILL, RECT_STROKE,
                THIN_HATCHED, BOLD_HATCHED, UNDER_LINE, UPPER_LINE)),
        SYMBOL(R.string.formula_symbol, arrayOf(BLANK)),
        DOT(R.string.formula_dot, arrayOf(BLANK)),
        BOTTOM_SINGLE_TEXT(R.string.formula_bottom_single_text,
                arrayOf(COLOR_PEN, RECT_FILL_BLUR, RECT_FILL, RECT_STROKE, ROUND_FILL, ROUND_STROKE,
                        THIN_HATCHED, BOLD_HATCHED, NEON_PEN, UNDER_LINE, UPPER_LINE, RANGE,
                        DASH_RANGE, ARROW, DASH_ARROW)),
        STICKER(R.string.formula_sticker, arrayOf(BLANK)),
        DATE_POINT(R.string.formula_date_point, arrayOf(BLANK));

        companion object {
            fun styleToFormula(style: Int) = values()[style % 100]
        }
    }

    fun draw(items : RealmResults<Record>?) {
        setCalendarData()
        makeRecordViewHolders(items)
        calculateRecordViewsPosition()
        drawRecordViewOnCalendarView()
    }

    fun refresh(items: RealmResults<Record>?, anim: Boolean) {
        withAnimtion = anim
        viewHolderList.forEach { holder ->
            holder.items.forEach { (it.parent as FrameLayout).removeView(it) }
        }
        viewHolderList.clear()
        viewLevelStatusMap.clear()
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

    private fun makeRecordViewHolders(items : RealmResults<Record>?) {
        items?.forEach{ record ->
            try{
                if(record.repeat.isNullOrEmpty()) {
                    makeRecordViewHolder(record)
                }else {
                    RepeatManager.makeRepeatInstances(record,
                            calendarView.calendarStartTime,
                            calendarView.calendarEndTime)
                            .forEach { makeRecordViewHolder(it) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }

        OsCalendarManager.getInstances(context, "",
                calendarView.calendarStartTime,
                calendarView.calendarEndTime).forEach { makeRecordViewHolder(it) }
        viewHolderList.sortWith(RecordCalendarComparator())
    }

    private fun makeRecordViewHolder(record: Record) {
        var startCellNum = ((record.dtStart - calStartTime) / DAY_MILL).toInt()
        var endCellNum = ((record.dtEnd - calStartTime) / DAY_MILL).toInt()
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

        var formula = record.getFormula()
        //var formula = DOT

        if(formula == MULTI_TEXT && endCellNum != startCellNum) { // 하루짜리가 아닐때 예외
            formula = SINGLE_TEXT
        }

        when(formula){
            SYMBOL, DOT, STICKER, DATE_POINT -> {
                (startCellNum .. endCellNum).forEach { cellnum ->
                    val holder =
                            viewHolderList.firstOrNull{ it.formula == formula && it.startCellNum == cellnum }
                                    ?: RecordViewHolder(formula, record, cellnum, cellnum).apply { viewHolderList.add(this) }
                    val recordView = holder.items.firstOrNull() ?:
                    RecordView(context, record, formula, cellnum, 1).apply {
                        childList = ArrayList()
                        holder.items.add(this)
                    }
                    recordView.childList?.add(record)
                }
            }
            else -> {
                val holder = RecordViewHolder(formula, record, startCellNum, endCellNum)
                var currentCell = holder.startCellNum
                var length = holder.endCellNum - holder.startCellNum + 1
                var margin = columns - currentCell % columns
                val size = 1 + (holder.endCellNum / columns - holder.startCellNum / columns)

                (0 until size).forEach { index ->
                    holder.items.add(RecordView(context, record, formula, currentCell,
                                    if (length <= margin) length else margin).apply {
                                currentCell += margin
                                length -= margin
                                margin = 7
                                when(holder.items.size) {
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
                            })}
                viewHolderList.add(holder)
            }
        }
    }

    private fun calculateRecordViewsPosition() {
        var currentFomula = BACKGROUND
        viewHolderList.forEach { viewHolder ->
            try{
                val formula = viewHolder.formula
                val status = viewLevelStatusMap[formula]?: ViewLevelStatus().apply { viewLevelStatusMap[formula] = this }

                if(formula != currentFomula) {
                    currentFomula = formula
                    when(currentFomula) {
                        SYMBOL -> addBottomMargin(dpToPx(1f), currentFomula)
                        DOT -> addBottomMargin(dpToPx(2f), currentFomula)
                        BOTTOM_SINGLE_TEXT -> {
                            addBottomMargin(dpToPx(15f), currentFomula)
                            computeBottomStackStartPos()
                        }
                        STICKER -> addBottomMargin(dpToPx(0f), currentFomula)
                        else -> {}
                    }
                }

                viewHolder.items.forEach {
                    it.mLeft = (minWidth * (it.cellNum % columns)) + CalendarView.calendarPadding
                    it.mRight = it.mLeft + (minWidth * it.length).toInt()
                    val viewHeight = it.getViewHeight()
                    when(formula) {
                        SINGLE_TEXT, BOTTOM_SINGLE_TEXT -> {
                            it.mTop = computeOrder(it, status) * viewHeight + rowHeightArray[it.cellNum / columns]
                        }
                        MULTI_TEXT, SYMBOL, DOT -> {
                            it.mTop = cellBottomArray[it.cellNum]
                        }
                        else -> {
                            it.mTop = 0f
                        }
                    }
                    it.mBottom = it.mTop + viewHeight
                    (it.cellNum until it.cellNum + it.length).forEach{ index ->
                        cellBottomArray[index] = Math.max(cellBottomArray[index], it.mBottom)
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
            rowHeightArray[index] = Math.max(minHeight - blockTypeSize - rowBottomMargin,
                    cellBottomArray.sliceArray(index*7..index*7+6).max() ?: 0f)
        }
    }

    private fun addBottomMargin(margin: Float, formula: Formula) {
        (0 until maxCellNum).forEach { index ->
            if(viewHolderList.any { it.formula == formula && index >= it.startCellNum && index <= it.endCellNum }){
                cellBottomArray[index] += margin
            }
        }
    }

    private fun drawRecordViewOnCalendarView() {
        if(withAnimtion) {
            TransitionManager.beginDelayedTransition(calendarView.calendarLy, makeChangeBounceTransition())
            withAnimtion = false
        }
        var calendarHeight = 0f

        calendarView.weekLys.forEachIndexed { index, weekLy ->
            if(index < rows) {
                val newHeight = rowHeightArray[index] + rowBottomMargin
                val finalHeight = Math.max(minHeight, newHeight)
                calendarHeight += finalHeight
                weekLy.layoutParams.height = finalHeight.toInt()
            }
        }

        viewHolderList.forEach { holder ->
            var lastAlpha = if(holder.record.isDone() &&
                    (AppStatus.checkedRecordDisplay == 1 || AppStatus.checkedRecordDisplay == 3)) 0.5f else 1f
            if(calendarView.lastUpdatedItem?.isValid == true && calendarView.lastUpdatedItem?.id == holder.record.id) { // 마지막 업데이트 오브젝트
                calendarView.lastUpdatedItem = null
                holder.items.forEach {
                    lastAlpha = if(isDateInMonth(it)) lastAlpha else AppStatus.outsideMonthAlpha
                    it.alpha = 0f
                    calendarView.recordsViews[it.cellNum].addView(it)
                    it.post { showInsertAnimation(it, lastAlpha) }
                }
            }else {
                holder.items.forEach {
                    it.alpha = if(isDateInMonth(it)) lastAlpha else AppStatus.outsideMonthAlpha
                    calendarView.recordsViews[it.cellNum].addView(it)
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
                ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1f),
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

    fun getViewsAtStart(cellNum: Int) : List<RecordView> {
        val result = ArrayList<RecordView>()
        viewHolderList.filter { cellNum in it.startCellNum..it.endCellNum }.forEach { holder ->
            holder.items.filter { it.cellNum == cellNum }.forEach { result.add(it) }
        }
        return result
    }

    fun getViewHolders(cellNum: Int) : List<RecordViewHolder> {
        return viewHolderList.filter { cellNum in it.startCellNum..it.endCellNum }
    }

    class RecordViewHolder(val formula: Formula, val record: Record,
                           val startCellNum: Int, val endCellNum: Int) {
        val items = ArrayList<RecordView>()
    }

    inner class ViewLevelStatus {
        var status = Array(maxCellNum){ "0" }
    }
}