package com.ayaan.twelvepages.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.l
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.WelcomeActivity
import com.ayaan.twelvepages.ui.view.CalendarView


class MonthlyCalendarWidget : AppWidgetProvider() {
    private var calendarView: CalendarView? = null
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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
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
        l("updateAppWidget")

        val rv = RemoteViews(context.packageName, R.layout.widget_monthly_calendar)
        appWidgetManager.getAppWidgetOptions(appWidgetId)?.let {
            val minWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val maxWidth = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val minHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maxHeight = it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            l("$minWidth x $minHeight $maxWidth x $maxHeight")
        }
        if(calendarView == null) calendarView = CalendarView(context)
        calendarView?.drawInWidget(rv, System.currentTimeMillis(), dowIds, dateIds)
        rv.setOnClickPendingIntent(R.id.rootLy, makeAppStartPendingIntent(context))
        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }

    private fun makeAppStartPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

