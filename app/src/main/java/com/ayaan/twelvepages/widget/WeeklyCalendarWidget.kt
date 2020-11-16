package com.ayaan.twelvepages.widget

import android.R.attr.bitmap
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.util.RecordCalendarComparator
import com.ayaan.twelvepages.manager.*
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.WidgetSettingActivity
import com.ayaan.twelvepages.ui.view.RecordView
import com.ayaan.twelvepages.ui.view.base.DateBgSample
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class WeeklyCalendarWidget : AppWidgetProvider() {
    private val widgetName = "weeklyWidget"
    private var context = App.context
    private val rowIds = arrayOf(R.id.row0, R.id.row1)
    private val dowIds = arrayOf(R.id.dowText0, R.id.dowText1, R.id.dowText2, R.id.dowText3, R.id.dowText4, R.id.dowText5, R.id.dowText6)
    private val lineIds = arrayOf(R.id.line0, R.id.line1)
    private val dateIds = arrayOf(
            R.id.dateText0, R.id.dateText1, R.id.dateText2, R.id.dateText3, R.id.dateText4, R.id.dateText5, R.id.dateText6,
            R.id.dateText7, R.id.dateText8, R.id.dateText9, R.id.dateText10, R.id.dateText11, R.id.dateText12, R.id.dateText13)
    private val stickers = arrayOf(
            R.id.sticker0, R.id.sticker1, R.id.sticker2, R.id.sticker3, R.id.sticker4, R.id.sticker5, R.id.sticker6,
            R.id.sticker7, R.id.sticker8, R.id.sticker9, R.id.sticker10, R.id.sticker11, R.id.sticker12, R.id.sticker13)
    private val recordRows = arrayOf(
            R.id.recordRow0, R.id.recordRow1, R.id.recordRow2, R.id.recordRow3, R.id.recordRow4,
            R.id.recordRow5, R.id.recordRow6, R.id.recordRow7, R.id.recordRow8, R.id.recordRow9,
            R.id.recordRow10, R.id.recordRow11, R.id.recordRow12, R.id.recordRow13, R.id.recordRow14,
            R.id.recordRow15, R.id.recordRow16, R.id.recordRow17, R.id.recordRow18, R.id.recordRow19,
            R.id.recordRow20, R.id.recordRow21, R.id.recordRow22, R.id.recordRow23, R.id.recordRow24,
            R.id.recordRow25, R.id.recordRow26, R.id.recordRow27, R.id.recordRow28, R.id.recordRow29)
    private val recordRvs = arrayOf(
            R.layout.widget_block_valid,
            R.layout.widget_block_valid_2,
            R.layout.widget_block_valid_3,
            R.layout.widget_block_valid_4,
            R.layout.widget_block_valid_5,
            R.layout.widget_block_valid_6,
            R.layout.widget_block_valid_7,
            R.layout.widget_block_valid_s,
            R.layout.widget_block_valid_2_s,
            R.layout.widget_block_valid_3_s,
            R.layout.widget_block_valid_4_s,
            R.layout.widget_block_valid_5_s,
            R.layout.widget_block_valid_6_s,
            R.layout.widget_block_valid_7_s)
    private val backgroudIds = arrayOf(
            R.id.background0, R.id.background1, R.id.background2, R.id.background3, R.id.background4, R.id.background5, R.id.background6,
            R.id.background7, R.id.background8, R.id.background9, R.id.background10, R.id.background11, R.id.background12, R.id.background13)

    companion object {
        private var weekPos = 0
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            intent?.data?.let {
                val data = it.toString()
                when (data) {
                    "moveWeek:left" -> {
                        weekPos--
                    }
                    "moveWeek:right" -> {
                        weekPos++
                    }
                    else -> {
                        weekPos = 0
                    }
                }
            }
            val appWidgetManager = AppWidgetManager.getInstance(ctx)
            val thisAppWidget = ComponentName(ctx.packageName, WeeklyCalendarWidget::class.java.name)
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
        font = Prefs.getInt("${widgetName}Font", 0)
        val rv = RemoteViews(context.packageName, if(font == 0) R.layout.widget_weekly_calendar else R.layout.widget_weekly_calendar_s)
        appWidgetManager.getAppWidgetOptions(appWidgetId)?.let {
            val minWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val maxWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val minHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maxHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            l("$maxWidth/$maxHeight")
        }
        l("?")
        val alpha = Prefs.getInt("${widgetName}Transparency", 100) * 255 / 100
        textColor = Prefs.getInt("${widgetName}TextColor", AppTheme.secondaryText)
        textSize = Prefs.getFloat("${widgetName}TextSize", 8f) + 1
        dateTextSize = Prefs.getFloat("${widgetName}DateTextSize", 12f)
        weekLineVisibility = Prefs.getInt("${widgetName}WeekLine", View.VISIBLE)
        rv.setOnClickPendingIntent(R.id.rootLy, makeAppStartPendingIntent(context))
        rv.setOnClickPendingIntent(R.id.settingBtn, buildSettingPendingIntent(context))
        rv.setOnClickPendingIntent(R.id.leftBtn, buildMoveMonthPendingIntent(context, "left"))
        rv.setOnClickPendingIntent(R.id.rightBtn, buildMoveMonthPendingIntent(context, "right"))
        rv.setInt(R.id.settingBtn, "setColorFilter", textColor)
        rv.setInt(R.id.leftBtn, "setColorFilter", textColor)
        rv.setInt(R.id.rightBtn, "setColorFilter", textColor)
        rv.setInt(R.id.backgroundView, "setAlpha", alpha)
        setCalendarView(rv)
        setRecordView(rv)
        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }

    private val viewHolderList = ArrayList<RecordCalendarAdapter.RecordViewHolder>()
    private val viewLevelStatus = ViewLevelStatus()
    private val columns = 7
    private var rows = 1
    private var calStartTime = 0L
    private var calEndTime = 0L
    private val todayCal: Calendar = Calendar.getInstance()
    private val tempCal: Calendar = Calendar.getInstance()
    private val weekCal: Calendar = Calendar.getInstance()
    private var sundayPos = 0
    private var saturdayPos = 6
    private val dateInfos = Array(14){ DateInfoManager.DateInfo()}
    private var dotCount = Array(14){ 0 }
    private var textColor = AppTheme.secondaryText
    private var textSize = 8f
    private var dateTextSize = 13f
    private var weekLineVisibility = View.VISIBLE
    private val maxRowItem = 15
    private var font = 0

    private fun setCalendarView(rv: RemoteViews) {
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = System.currentTimeMillis()
        tempCal.add(Calendar.DATE, 7 * weekPos)
        weekCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)
        var todayPos = tempCal.get(Calendar.DAY_OF_WEEK) - AppStatus.startDayOfWeek
        if (todayPos < 0) { todayPos += 7 }
        val showNextWeek = Prefs.getInt("${widgetName}ShowNextWeek", 0)
        rows = 1 + showNextWeek

        if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            sundayPos = 0
            saturdayPos = 6
        } else {
            sundayPos = 8 - AppStatus.startDayOfWeek
            saturdayPos = 7 - AppStatus.startDayOfWeek
        }

        tempCal.add(Calendar.DATE, -todayPos)

        weekCal.firstDayOfWeek = Calendar.SUNDAY
        weekCal.minimalDaysInFirstWeek = 4

        rv.setTextColor(R.id.monthlyText, textColor)
        if(weekCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) {
            rv.setTextViewText(R.id.monthlyText, AppDateFormat.month.format(weekCal.time)
                    + " " + String.format(context.getString(R.string.weekNum), weekCal.get(Calendar.WEEK_OF_MONTH)))
        }else {
            rv.setTextViewText(R.id.monthlyText, AppDateFormat.ym.format(weekCal.time)
                    + " " + String.format(context.getString(R.string.weekNum), weekCal.get(Calendar.WEEK_OF_MONTH)))
        }

        lineIds.forEach {
            if(weekLineVisibility == View.VISIBLE) {
                rv.setViewVisibility(it, View.VISIBLE)
            }else {
                rv.setViewVisibility(it, View.INVISIBLE)
            }
        }

        for(i in 0..1) {
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
                        rv.setTextViewTextSize(dowIds[cellNum], TypedValue.COMPLEX_UNIT_DIP, dateTextSize - 1)
                    }

                    rv.setTextViewText(dateIds[cellNum], String.format("%02d", tempCal.get(Calendar.DATE)))
                    rv.setTextViewTextSize(dateIds[cellNum], TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
                    rv.setTextColor(dateIds[cellNum], color)

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
        viewLevelStatus.status = Array(14){ "0" }
        dotCount = Array(14){ 0 }
        recordRows.forEachIndexed { index, id ->
            rv.removeAllViews(id)
            if(index % maxRowItem > 5) {
                rv.setViewVisibility(id, View.GONE)
            }
        }
        stickers.forEach { rv.setViewVisibility(it, View.GONE) }
        //backgroudIds.forEach { rv.setImageViewBitmap(it, null) }

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

                OsCalendarManager.getInstances(context, "", calStartTime, calEndTime).forEach { makeRecordViewHolder(it) }
                viewHolderList.sortWith(RecordCalendarComparator())
                viewHolderList.forEach { holder ->
                    val lastAlpha = if(holder.record.isDone() &&
                            (AppStatus.checkedRecordDisplay == 1 || AppStatus.checkedRecordDisplay == 3)) 70 else 255

                    val formula = holder.formula

                    holder.items.forEach { view ->
                        if((formula == RecordCalendarAdapter.Formula.SINGLE_TEXT
                                || formula == RecordCalendarAdapter.Formula.MULTI_TEXT
                                || formula == RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                                && view.length > 0) {
                            val order = computeOrder(view.cellNum, view.length, viewLevelStatus)
                            if (order < maxRowItem - 1) {
                                val colorKey = view.record.colorKey
                                val color = ColorManager.getColor(colorKey)
                                val fontColor = ColorManager.getFontColor(color)
                                val title = view.record.getTitleInCalendar()
                                val recordRv = getRecordRemoteView(view.length, view.cellNum)

                                if (view.record.isSetCheckBox) {
                                    recordRv.setViewVisibility(R.id.checkImg, View.VISIBLE)
                                    recordRv.setInt(R.id.checkImg, "setAlpha", lastAlpha)
                                    if(view.shape.fontColor) {
                                        recordRv.setInt(R.id.checkImg, "setColorFilter", color)
                                    }else {
                                        recordRv.setInt(R.id.checkImg, "setColorFilter", AppTheme.primaryText)
                                    }
                                    if (view.record.isDone()) {
                                        recordRv.setImageViewResource(R.id.checkImg, R.drawable.checked_small)
                                        recordRv.setTextViewText(R.id.valid_text, title)
                                    } else {
                                        recordRv.setImageViewResource(R.id.checkImg, R.drawable.uncheck_small)
                                        recordRv.setTextViewText(R.id.valid_text, title)
                                    }
                                }else{
                                    recordRv.setViewVisibility(R.id.checkImg, View.GONE)
                                    recordRv.setTextViewText(R.id.valid_text, " $title")
                                }

                                var isTempAlpha = false

                                when(view.shape) {
                                    RecordView.Shape.COLOR_PEN -> {
                                        if(view.length > 1) {
                                            recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_rect_fill)
                                            isTempAlpha = true
                                        }
                                    }
                                    RecordView.Shape.RECT_FILL -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_rect_fill)
                                    RecordView.Shape.RECT_STROKE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_rect_stroke)
                                    RecordView.Shape.ROUND_FILL -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_round_rect_fill)
                                    RecordView.Shape.ROUND_STROKE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_round_rect_stroke)
                                    RecordView.Shape.UPPER_LINE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_top_line)
                                    RecordView.Shape.UNDER_LINE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_under_line)
                                    RecordView.Shape.RANGE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_range)
                                    RecordView.Shape.DASH_RANGE -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_range_dash)
                                    RecordView.Shape.ARROW -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_range)
                                    RecordView.Shape.DASH_ARROW -> recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_range_dash)
                                    else -> {
                                        recordRv.setImageViewResource(R.id.valid_img, R.drawable.wg_rv_rect_fill)
                                        isTempAlpha = true
                                    }
                                }

                                if(isTempAlpha) {
                                    recordRv.setInt(R.id.valid_img, "setColorFilter", color and 0x00FFFFFF or 0x20000000)
                                }else {
                                    recordRv.setInt(R.id.valid_img, "setColorFilter", color)
                                }


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
                                            recordRv.setTextColor(R.id.valid_text, textColor and 0x00FFFFFF or -0x80000000)
                                        }
                                    }else {
                                        if(view.shape.fontColor) {
                                            recordRv.setTextColor(R.id.valid_text, color)
                                        }else {
                                            recordRv.setTextColor(R.id.valid_text, textColor)
                                        }
                                    }
                                }

                                recordRv.setTextViewTextSize(R.id.valid_text, TypedValue.COMPLEX_UNIT_DIP, textSize)

                                val recordRowId = recordRows[view.cellNum / columns * maxRowItem + order]
                                rv.addView(recordRowId, recordRv)
                                rv.setViewVisibility(recordRowId, View.VISIBLE)
                            }else {
                                if(view.length > 0) {
                                    (view.cellNum until view.cellNum + view.length).forEach {
                                        dotCount[it] = dotCount[it] + 1
                                    }
                                }
                            }
                        }else if(formula == RecordCalendarAdapter.Formula.STICKER) {
                            rv.setViewVisibility(stickers[view.cellNum], View.VISIBLE)
                            rv.setImageViewResource(stickers[view.cellNum], view.record.getSticker()?.resId ?: R.drawable.help)
                            rv.setInt(stickers[view.cellNum], "setAlpha", lastAlpha)
                            rv.setViewPadding(stickers[view.cellNum], 0, 0, dpToPx(5), 0)
                        }else if(formula == RecordCalendarAdapter.Formula.BACKGROUND) {
//                            val proxy = Bitmap.createBitmap(dpToPx(30f).toInt(), dpToPx(100f).toInt(), Bitmap.Config.ARGB_8888)
//                            val c = Canvas(proxy)
//                            DateBgSample.draw(c, Paint(), view.record, dpToPx(30f), dpToPx(100f))
//                            rv.setImageViewBitmap(backgroudIds[view.cellNum], proxy)
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
                        val recordRv = getRecordRemoteView(1, cellNum)
                        recordRv.setTextViewText(R.id.valid_text, " +$count")
                        recordRv.setTextColor(R.id.valid_text, AppTheme.secondaryText)

                        val recordRowId = recordRows[cellNum / columns * maxRowItem + min(order, maxRowItem - 1)]
                        rv.addView(recordRowId, recordRv)
                        rv.setViewVisibility(recordRowId, View.VISIBLE)
                    }
                }
            }
        }
        realm.close()
    }

    private fun getRecordRemoteView(length: Int, cellNum: Int): RemoteViews {
        val recordRv = RemoteViews(context.packageName, R.layout.widget_block)
        (0 until 7 - length + 1).forEach {
            if(it == cellNum % columns) {
                recordRv.addView(R.id.widgetBlock, RemoteViews(context.packageName, recordRvs[font * 7 + length - 1]))
            }else {
                recordRv.addView(R.id.widgetBlock, RemoteViews(context.packageName, R.layout.widget_block_blank))
            }
        }
        return recordRv
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
            RecordCalendarAdapter.Formula.STICKER -> {
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

    private fun getDateTextColor(cellNum: Int, isHoli: Boolean, isSelected: Boolean) : Int {
        val color =  if((isHoli || cellNum % columns == sundayPos) && CalendarManager.sundayColor != CalendarManager.dateColor) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos && CalendarManager.saturdayColor != CalendarManager.dateColor) {
            CalendarManager.saturdayColor
        }else {
            textColor
        }

        return if(color == CalendarManager.dateColor && isSelected) {
            textColor
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
        var status = Array(14){ "0" }
    }

    private fun makeAppStartPendingIntent(context: Context): PendingIntent? {
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return null
    }

    private fun buildSettingPendingIntent(context: Context): PendingIntent? {
        val intent = Intent(context, WidgetSettingActivity::class.java)
        intent.data = Uri.parse(widgetName)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildMoveMonthPendingIntent(context: Context, direction: String): PendingIntent? {
        val intent = Intent(context, WeeklyCalendarWidget::class.java)
        intent.data = Uri.parse("moveWeek:$direction")
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

