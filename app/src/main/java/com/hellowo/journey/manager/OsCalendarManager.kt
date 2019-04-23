package com.hellowo.journey.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.hellowo.journey.AppTheme
import com.hellowo.journey.model.Record
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object OsCalendarManager {
    private const val INDEX_ID = 0
    private const val INDEX_TITLE = 1
    private const val INDEX_DTSTART = 2
    private const val INDEX_DTEND = 3
    private const val INDEX_ALLDAY = 4
    private const val INDEX_CAL_COLOR = 5
    private const val INDEX_LOCATION = 6
    private const val INDEX_DESCRIPTION = 7
    private const val INDEX_EVENT_COLOR = 8

    private const val INDEX_CALENDAR_DISPLAY_NAME = 1
    private const val INDEX_CALENDAR_COLOR = 2
    private const val INDEX_CALENDAR_ACCOUNT_NAME = 4

    private val CALENDAR_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

    private val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_COLOR,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_COLOR,
            CalendarContract.Instances.RRULE,
            CalendarContract.Instances.RDATE,
            CalendarContract.Instances.HAS_ALARM,
            CalendarContract.Instances.HAS_ATTENDEE_DATA,
            CalendarContract.Instances.EVENT_TIMEZONE,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.ORIGINAL_INSTANCE_TIME)

    private val EVENT_PROJECTION = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.CALENDAR_COLOR,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_COLOR,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.RDATE,
            CalendarContract.Events.HAS_ALARM,
            CalendarContract.Events.HAS_ATTENDEE_DATA,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.ORIGINAL_INSTANCE_TIME)

    init {}

    class OsCalendar(val id: Long, val title: String, val color: Int, val accountName: String) {
        override fun toString(): String {
            return "OsCalendar(id=$id, title='$title', color=$color)"
        }
    }

    private fun checkPermission(context: Context)
            = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun getCalendarList(context: Context): List<OsCalendar> {
        val categoryList = ArrayList<OsCalendar>()
        if(checkPermission(context)) {
            val cr = context.contentResolver
            val cur = cr.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, null)

            if (cur != null && cur.count > 0) {
                while (!cur.isLast) {
                    cur.moveToNext()
                    val osCalendar = OsCalendar(cur.getLong(INDEX_ID),
                            cur.getString(INDEX_CALENDAR_DISPLAY_NAME),
                            cur.getInt(INDEX_CALENDAR_COLOR),
                            cur.getString(INDEX_CALENDAR_ACCOUNT_NAME))
                    categoryList.add(osCalendar)
                }
            }
            cur?.close()
        }
        return categoryList
    }

    @SuppressLint("MissingPermission")
    fun getInstances(context: Context, keyWord: String, startMillis: Long, endMillis: Long): List<Record> {
        val list = ArrayList<Record>()
        if(checkPermission(context)) {
            val cr = context.contentResolver
            val selection: String
            val selectionArgs: Array<String>?
            val cur: Cursor?

            val osCalendarIds = Prefs.getStringSet("osCalendarIds", HashSet<String>())
            if (osCalendarIds.size > 0) {
                val categoryQuery = osCalendarIds.joinToString(" OR ") { "(${CalendarContract.Instances.CALENDAR_ID}=$it)" }

                if (TextUtils.isEmpty(keyWord)) {
                    selection = categoryQuery
                    selectionArgs = null
                } else {
                    selection = ("((" + CalendarContract.Instances.TITLE + " LIKE ?) OR ("
                            + CalendarContract.Instances.DESCRIPTION + " LIKE ?))"
                            + " AND (" + categoryQuery + ")")
                    selectionArgs = arrayOf("%$keyWord%", "%$keyWord%")
                }

                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder, startMillis)
                ContentUris.appendId(builder, endMillis)

                cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs,
                        CalendarContract.Instances.BEGIN + " asc")

                if (cur != null && cur.count > 0) {
                    while (!cur.isLast) {
                        cur.moveToNext()
                        try {
                            list.add(makeTimeObject(cur))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                cur?.close()
            }
        }
        return list
    }

    @SuppressLint("MissingPermission")
    fun searchEvents(context: Context, keyWord: String): List<Record> {
        val list = ArrayList<Record>()
        if(checkPermission(context) && !keyWord.isEmpty()) {
            val cr = context.contentResolver
            val selection: String
            val selectionArgs: Array<String>?
            val cur: Cursor?

            val osCalendarIds = Prefs.getStringSet("osCalendarIds", HashSet<String>())
            if (osCalendarIds.size > 0) {
                val categoryQuery = osCalendarIds.joinToString(" OR ") { "(${CalendarContract.Events.CALENDAR_ID}=$it)" }

                selection = ("((" + CalendarContract.Events.TITLE + " LIKE ?) OR ("
                        + CalendarContract.Events.DESCRIPTION + " LIKE ?))"
                        + " AND (" + categoryQuery + ")")
                selectionArgs = arrayOf("%$keyWord%", "%$keyWord%")

                val builder = CalendarContract.Events.CONTENT_URI.buildUpon()

                cur = cr.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs,
                        CalendarContract.Instances.DTSTART + " asc")

                if (cur != null && cur.count > 0) {
                    while (!cur.isLast) {
                        cur.moveToNext()
                        try {
                            list.add(makeTimeObject(cur))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                cur?.close()
            }
        }
        return list
    }

    private val timeZone = TimeZone.getDefault().rawOffset

    private fun makeTimeObject(cur: Cursor) : Record{
        val block = Record(
                id = "osInstance::${cur.getLong(INDEX_ID)}",
                style = Record.Style.RECT_FILL.ordinal,
                title = cur.getString(INDEX_TITLE),
                colorKey = AppTheme.getColorKey(if(cur.getInt(INDEX_EVENT_COLOR) != 0) cur.getInt(INDEX_EVENT_COLOR)
                else cur.getInt(INDEX_CAL_COLOR)),
                location = cur.getString(INDEX_LOCATION),
                description = cur.getString(INDEX_DESCRIPTION),
                allday = cur.getInt(INDEX_ALLDAY) == 1,
                dtStart = cur.getLong(INDEX_DTSTART),
                dtEnd = cur.getLong(INDEX_DTEND))
        if(block.allday) {
            block.dtUpdated = block.dtStart
            block.dtCreated = block.dtEnd
            block.dtStart -= timeZone
            block.dtEnd -= (timeZone + 1)
        }
        return block
    }
}