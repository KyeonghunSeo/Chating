package com.ayaan.twelvepages.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.util.RecordCalendarComparator
import com.ayaan.twelvepages.manager.*
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class MonthlyCalendarWidget : AppWidgetProvider() {
    private var context = App.context
    private val rowIds = arrayOf(
            R.id.row0, R.id.row1, R.id.row2, R.id.row3, R.id.row4, R.id.row5)
    private val dowIds = arrayOf(
            R.id.dowText0, R.id.dowText1, R.id.dowText2, R.id.dowText3, R.id.dowText4, R.id.dowText5, R.id.dowText6)
    private val dateIds = arrayOf(
            R.id.dateText0, R.id.dateText1, R.id.dateText2, R.id.dateText3, R.id.dateText4, R.id.dateText5, R.id.dateText6,
            R.id.dateText7, R.id.dateText8, R.id.dateText9, R.id.dateText10, R.id.dateText11, R.id.dateText12, R.id.dateText13,
            R.id.dateText14, R.id.dateText15, R.id.dateText16, R.id.dateText17, R.id.dateText18, R.id.dateText19, R.id.dateText20,
            R.id.dateText21, R.id.dateText22, R.id.dateText23, R.id.dateText24, R.id.dateText25, R.id.dateText26, R.id.dateText27,
            R.id.dateText28, R.id.dateText29, R.id.dateText30, R.id.dateText31, R.id.dateText32, R.id.dateText33, R.id.dateText34,
            R.id.dateText35, R.id.dateText36, R.id.dateText37, R.id.dateText38, R.id.dateText39, R.id.dateText40, R.id.dateText41)
    private val stickers = arrayOf(
            R.id.sticker0, R.id.sticker1, R.id.sticker2, R.id.sticker3, R.id.sticker4, R.id.sticker5, R.id.sticker6,
            R.id.sticker7, R.id.sticker8, R.id.sticker9, R.id.sticker10, R.id.sticker11, R.id.sticker12, R.id.sticker13,
            R.id.sticker14, R.id.sticker15, R.id.sticker16, R.id.sticker17, R.id.sticker18, R.id.sticker19, R.id.sticker20,
            R.id.sticker21, R.id.sticker22, R.id.sticker23, R.id.sticker24, R.id.sticker25, R.id.sticker26, R.id.sticker27,
            R.id.sticker28, R.id.sticker29, R.id.sticker30, R.id.sticker31, R.id.sticker32, R.id.sticker33, R.id.sticker34,
            R.id.sticker35, R.id.sticker36, R.id.sticker37, R.id.sticker38, R.id.sticker39, R.id.sticker40, R.id.sticker41)
    private val recordRows = arrayOf(
            R.id.recordRow0, R.id.recordRow1, R.id.recordRow2, R.id.recordRow3, R.id.recordRow4,
            R.id.recordRow5, R.id.recordRow6, R.id.recordRow7, R.id.recordRow8, R.id.recordRow9,
            R.id.recordRow10, R.id.recordRow11, R.id.recordRow12, R.id.recordRow13, R.id.recordRow14,
            R.id.recordRow15, R.id.recordRow16, R.id.recordRow17, R.id.recordRow18, R.id.recordRow19,
            R.id.recordRow20, R.id.recordRow21, R.id.recordRow22, R.id.recordRow23, R.id.recordRow24,
            R.id.recordRow25, R.id.recordRow26, R.id.recordRow27, R.id.recordRow28, R.id.recordRow29)
    private val recordRvs = arrayOf(
            arrayOf(R.layout.widget_block_7_1_1, R.layout.widget_block_7_1_2, R.layout.widget_block_7_1_3, R.layout.widget_block_7_1_4,
                    R.layout.widget_block_7_1_5, R.layout.widget_block_7_1_6, R.layout.widget_block_7_1_7),
            arrayOf(R.layout.widget_block_7_2_1, R.layout.widget_block_7_2_2, R.layout.widget_block_7_2_3, R.layout.widget_block_7_2_4,
                    R.layout.widget_block_7_2_5, R.layout.widget_block_7_2_6),
            arrayOf(R.layout.widget_block_7_3_1, R.layout.widget_block_7_3_2, R.layout.widget_block_7_3_3, R.layout.widget_block_7_3_4,
                    R.layout.widget_block_7_3_5),
            arrayOf(R.layout.widget_block_7_4_1, R.layout.widget_block_7_4_2, R.layout.widget_block_7_4_3, R.layout.widget_block_7_4_4),
            arrayOf(R.layout.widget_block_7_5_1, R.layout.widget_block_7_5_2, R.layout.widget_block_7_5_3),
            arrayOf(R.layout.widget_block_7_6_1, R.layout.widget_block_7_6_2),
            arrayOf(R.layout.widget_block_full)
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            val appWidgetManager = AppWidgetManager.getInstance(ctx)
            val thisAppWidget = ComponentName(ctx.packageName, MonthlyCalendarWidget::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            super.onReceive(ctx, intent)
            onUpdate(ctx, appWidgetManager, appWidgetIds)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if(context != null && appWidgetManager != null) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        this.context = context
        val rv = RemoteViews(context.packageName, R.layout.widget_monthly_calendar)
        appWidgetManager.getAppWidgetOptions(appWidgetId)?.let {
//            val minWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
//            val maxWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val minHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maxHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        }
        rv.setOnClickPendingIntent(R.id.rootLy, makeAppStartPendingIntent(context))
        rv.setInt(R.id.backgroundView, "setAlpha", 255)
        setCalendarView(rv)
        setRecordView(rv)
        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }

    private val viewHolderList = ArrayList<RecordCalendarAdapter.RecordViewHolder>()
    private val viewLevelStatus = ViewLevelStatus()
    private val columns = 7
    private var rows = 0
    private var calStartTime = 0L
    private var calEndTime = 0L
    private var startCellNum = 0
    private var endCellNum = 0
    private val todayCal: Calendar = Calendar.getInstance()
    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    private var sundayPos = 0
    private var saturdayPos = 6
    private val dateInfos = Array(42){ DateInfoManager.DateInfo()}
    private var dotCount = Array(42){ 0 }

    private fun setCalendarView(rv: RemoteViews) {
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = System.currentTimeMillis()
        tempCal.set(Calendar.DATE, 1)
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - AppStatus.startDayOfWeek
        if (startCellNum < 0) { startCellNum += 7 }
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0

        if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            sundayPos = 0
            saturdayPos = 6
        } else {
            sundayPos = 8 - AppStatus.startDayOfWeek
            saturdayPos = 7 - AppStatus.startDayOfWeek
        }

        tempCal.add(Calendar.DATE, -startCellNum)

        rv.setTextViewText(R.id.monthlyText, AppDateFormat.month.format(monthCal.time))
        rv.setTextViewText(R.id.yearText, monthCal.get(Calendar.YEAR).toString())

        for(i in 0..5) {
            if(i < rows) {
                rv.setViewVisibility(rowIds[i], View.VISIBLE)
                for (j in 0..6){
                    val cellNum = i*7 + j
                    if(cellNum == 0) {
                        calStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calEndTime = tempCal.timeInMillis
                    }

                    DateInfoManager.getHoliday(dateInfos[cellNum], tempCal)
                    val color = getDateTextColor(cellNum, dateInfos[cellNum].holiday?.isHoli == true, false)

                    if(i == 0) {
                        rv.setTextViewText(dowIds[cellNum], AppDateFormat.simpleDow.format(tempCal.time))
                        rv.setTextColor(dowIds[cellNum], color)
                    }

                    rv.setTextViewText(dateIds[cellNum], String.format("%02d", tempCal.get(Calendar.DATE)))
                    if(!isDateInMonth(cellNum, 1) && AppStatus.outsideMonthAlpha == 0f) {
                        rv.setTextColor(dateIds[cellNum], Color.TRANSPARENT)
                    }else {
                        rv.setTextColor(dateIds[cellNum], color)
                    }

                    if(isToday(tempCal)) {
                        rv.setInt(dateIds[cellNum], "setBackgroundResource", R.drawable.today_indi)
                    }else {
                        rv.setInt(dateIds[cellNum], "setBackgroundResource", R.drawable.blank)
                    }

                    tempCal.add(Calendar.DATE, 1)
                }
            }else {
                rv.setViewVisibility(rowIds[i], View.GONE)
            }
        }

    }

    private fun setRecordView(rv: RemoteViews) {
        viewHolderList.clear()
        viewLevelStatus.status = Array(42){ "0" }
        dotCount = Array(42){ 0 }
        recordRows.forEach { rv.removeAllViews(it) }
        stickers.forEach { rv.setViewVisibility(it, View.GONE) }

        val realm = Realm.getDefaultInstance()
        val folder = realm.where(Folder::class.java).equalTo("id", "calendar").findFirst()
        folder?.let {
            RecordManager.getRecordList(calStartTime, calEndTime, folder).let { items ->
                items.forEach{ record ->
                    try{
                        if(record.repeat.isNullOrEmpty()) {
                            makeRecordViewHolder(record)
                        }else {
                            RepeatManager.makeRepeatInstances(record,
                                    calStartTime,
                                    calEndTime)
                                    .forEach { makeRecordViewHolder(it) }
                        }
                    }catch (e: Exception){ e.printStackTrace() }
                }

                OsCalendarManager.getInstances(context, "",
                        calStartTime,
                        calEndTime).forEach { makeRecordViewHolder(it) }
                viewHolderList.sortWith(RecordCalendarComparator())
                viewHolderList.forEach { holder ->
                    var lastAlpha = if(holder.record.isDone() &&
                            (AppStatus.checkedRecordDisplay == 1 || AppStatus.checkedRecordDisplay == 3)) 70 else 255

                    val formula = holder.formula

                    holder.items.forEach { view ->
                        lastAlpha = if(isDateInMonth(view.cellNum, view.length)) lastAlpha else (AppStatus.outsideMonthAlpha * 255).toInt()
                        if((formula == RecordCalendarAdapter.Formula.SINGLE_TEXT
                                || formula == RecordCalendarAdapter.Formula.MULTI_TEXT
                                || formula == RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                                && view.length > 0) {
                            val order = computeOrder(view.cellNum, view.length, viewLevelStatus)
                            if (order < 4) {
                                val colorKey = view.record.colorKey
                                val color = ColorManager.getColor(colorKey)
                                val fontColor = ColorManager.getFontColor(color)
                                val title = view.record.getTitleInCalendar()
                                val recordRv = RemoteViews(context.packageName, recordRvs[view.length - 1][view.cellNum % columns])
                                if (view.record.isSetCheckBox) {
                                    recordRv.setInt(R.id.checkImg, "setAlpha", lastAlpha)
                                    if(view.shape.fontColor) {
                                        recordRv.setInt(R.id.checkImg, "setColorFilter", color)
                                    }else {
                                        recordRv.setInt(R.id.checkImg, "setColorFilter", AppTheme.primaryText)
                                    }
                                    if (view.record.isDone()) {
                                        recordRv.setImageViewResource(R.id.checkImg, R.drawable.check)
                                        recordRv.setTextViewText(R.id.valid_text, "     $title")
                                    } else {
                                        recordRv.setImageViewResource(R.id.checkImg, R.drawable.uncheck)
                                        recordRv.setTextViewText(R.id.valid_text, "     $title")
                                    }
                                }else{
                                    recordRv.setTextViewText(R.id.valid_text, " $title")
                                }

                                recordRv.setImageViewResource(R.id.valid_img, R.drawable.normal_rect_fill)
                                recordRv.setInt(R.id.valid_img, "setColorFilter", color)
                                if(view.shape.isFillColor) {
                                    recordRv.setTextColor(R.id.valid_text, fontColor)
                                    recordRv.setInt(R.id.valid_img, "setAlpha", lastAlpha)
                                }else {
                                    if(lastAlpha == 0) {
                                        recordRv.setTextColor(R.id.valid_text, Color.TRANSPARENT)
                                    }else if(lastAlpha in 0..100){
                                        if(view.shape.fontColor) {
                                            recordRv.setTextColor(R.id.valid_text, color and 0x00FFFFFF or -0x80000000)
                                        }else {
                                            recordRv.setTextColor(R.id.valid_text, AppTheme.primaryText and 0x00FFFFFF or -0x80000000)
                                        }
                                    }else {
                                        if(view.shape.fontColor) {
                                            recordRv.setTextColor(R.id.valid_text, color)
                                        }else {
                                            recordRv.setTextColor(R.id.valid_text, AppTheme.primaryText)
                                        }
                                    }
                                    recordRv.setInt(R.id.valid_img, "setAlpha", (lastAlpha * 0.05f).toInt())
                                }
                                rv.addView(recordRows[view.cellNum / columns * 5 + order], recordRv)
                            }
                        }else if(formula == RecordCalendarAdapter.Formula.STICKER) {
                            rv.setViewVisibility(stickers[view.cellNum], View.VISIBLE)
                            rv.setImageViewResource(stickers[view.cellNum], view.record.getSticker()?.resId ?: R.drawable.help)
                            rv.setInt(stickers[view.cellNum], "setAlpha", lastAlpha)
                            rv.setViewPadding(stickers[view.cellNum], 0, 0, dpToPx(5), 0)
                        }else {
                            if(formula == RecordCalendarAdapter.Formula.DOT) {
                                dotCount[view.cellNum] = dotCount[view.cellNum] + (view.childList?.size ?: 0)
                            }else {
                                if(view.length > 0) {
                                    (view.cellNum until view.cellNum + view.length).forEach {
                                        dotCount[it] = dotCount[it] + 1
                                    }
                                }
                            }
                        }
                    }
                }

                dotCount.forEachIndexed { cellNum, count ->
                    if(count > 0) {
                        val order = computeOrder(cellNum, 1, viewLevelStatus)
                        val recordRv = RemoteViews(context.packageName, recordRvs[0][cellNum % columns])
                        recordRv.setTextViewText(R.id.valid_text, " +$count")
                        recordRv.setTextColor(R.id.valid_text, AppTheme.secondaryText)
                        rv.addView(recordRows[cellNum / columns * 5 + min(order, 4)], recordRv)
                    }
                }
            }
        }
        realm.close()
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
        if(endCellNum > rows * columns - 1) {
            endCellNum = rows * columns - 1
            rOpen = true
        }

        var formula = record.getFormula()

        if(formula == RecordCalendarAdapter.Formula.MULTI_TEXT && endCellNum != startCellNum) { // 하루짜리가 아닐때 예외
            formula = RecordCalendarAdapter.Formula.SINGLE_TEXT
        }

        when(formula){
            RecordCalendarAdapter.Formula.SYMBOL,
            RecordCalendarAdapter.Formula.DOT,
            RecordCalendarAdapter.Formula.STICKER,
            RecordCalendarAdapter.Formula.DATE_POINT -> {
                (startCellNum .. endCellNum).forEach { cellnum ->
                    val holder =
                            viewHolderList.firstOrNull{ it.formula == formula && it.startCellNum == cellnum }
                                    ?: RecordCalendarAdapter.RecordViewHolder(formula, record, cellnum, cellnum).apply { viewHolderList.add(this) }
                    val recordView = holder.items.firstOrNull() ?:
                    RecordView(context, record, formula, cellnum, 1).apply {
                        childList = ArrayList()
                        holder.items.add(this)
                    }
                    recordView.childList?.add(record)
                }
            }
            else -> {
                val holder = RecordCalendarAdapter.RecordViewHolder(formula, record, startCellNum, endCellNum)
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

    private fun isDateInMonth(cellNum: Int, length: Int) = cellNum in startCellNum..endCellNum
            || cellNum + length - 1 in startCellNum..endCellNum

    private fun getDateTextColor(cellNum: Int, isHoli: Boolean, isSelected: Boolean) : Int {
        val color =  if(isHoli || cellNum % columns == sundayPos) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }

        return if(color == CalendarManager.dateColor && isSelected) {
            CalendarManager.selectedDateColor
        }else {
            color
        }
    }

    private fun computeOrder(cellNum: Int, length: Int, status: ViewLevelStatus): Int {
        var order = 0
        for (i in cellNum until cellNum + length) {
            val s = StringBuilder(status.status[i])

            if(i == cellNum) {
                var findPosition = false
                order = s.indexOf("0") // 빈공간 찾기
                if(order == -1) order = s.length // 빈공간이 없으면 가장 마지막 순서

                if(length > 1) {
                    while(!findPosition) {
                        var breakPoint = false
                        for (j in cellNum until cellNum + length) {
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

    inner class ViewLevelStatus {
        var status = Array(42){ "0" }
    }

    private fun makeAppStartPendingIntent(context: Context): PendingIntent? {
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return null
    }
}

