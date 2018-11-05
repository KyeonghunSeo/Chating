package com.hellowo.journey.calendar

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.provider.CalendarContract
import android.text.TextUtils
import com.hellowo.journey.l
import com.hellowo.journey.model.TimeObject
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object OsCalendarManager {
    private val INDEX_ID = 0
    private val INDEX_TITLE = 1
    private val INDEX_DTSTART = 2
    private val INDEX_DTEND = 3
    private val INDEX_ALLDAY = 4
    private val INDEX_DURATION = 5
    private val INDEX_LOCATION = 6
    private val INDEX_DESCRIPTION = 7
    private val INDEX_EVENT_COLOR = 8
    private val INDEX_RRULE = 9
    private val INDEX_RDATE = 10
    private val INDEX_HAS_ALARM = 11
    private val INDEX_HAS_ATTENDEE_DATA = 12
    private val INDEX_EVENT_TIMEZONE = 13
    private val INDEX_CALENDAR_ID = 14
    private val INDEX_ORIGINAL_DTSTART = 15
    private val INDEX_ORIGINAL_DTEND = 16

    private val INDEX_CALENDAR_DISPLAY_NAME = 1
    private val INDEX_CALENDAR_COLOR = 2
    private val INDEX_ACCOUNT_TYPE = 3
    private val INDEX_ACCOUNT_NAME = 4
    private val INDEX_OWNER_ACCOUNT = 5
    private val INDEX_VISIBLE = 6
    private val INDEX_CALENDAR_ACCESS_LEVEL = 7

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
            CalendarContract.Instances.DURATION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_COLOR,
            CalendarContract.Instances.RRULE,
            CalendarContract.Instances.RDATE,
            CalendarContract.Instances.HAS_ALARM,
            CalendarContract.Instances.HAS_ATTENDEE_DATA,
            CalendarContract.Instances.EVENT_TIMEZONE,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.DTSTART,
            CalendarContract.Instances.DTEND,
            CalendarContract.Instances.ORIGINAL_INSTANCE_TIME)

    init {}

    class OsCalendar(val id: Long, val title: String, val color: Int) {
        override fun toString(): String {
            return "OsCalendar(id=$id, title='$title', color=$color)"
        }
    }

    @SuppressLint("MissingPermission")
    fun getCalendarList(context: Context): List<OsCalendar> {
        val cr = context.contentResolver
        val categoryList = ArrayList<OsCalendar>()
        val cur = cr.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, null)

        if (cur != null && cur.count > 0) {
            while (!cur.isLast) {
                cur.moveToNext()
                val osCalendar = OsCalendar(cur.getLong(INDEX_ID),
                        cur.getString(INDEX_CALENDAR_DISPLAY_NAME),
                        cur.getInt(INDEX_CALENDAR_COLOR))
                l(osCalendar.toString())
                categoryList.add(osCalendar)
            }
        }
        cur?.close()
        return categoryList
    }

    @SuppressLint("MissingPermission")
    fun getInstances(context: Context, keyWord: String, startMillis: Long, endMillis: Long): List<TimeObject> {
        val cr = context.contentResolver
        val instance_list = ArrayList<TimeObject>()
        val selection: String
        val selectionArgs: Array<String>
        val cur: Cursor?

        val osCalendarIds = Prefs.getStringSet("osCalendarIds", HashSet<String>())
        //if (osCalendarIds.size > 0) {
        if (true) {
            val categoryQuery = osCalendarIds.joinToString(" OR ") { "(${CalendarContract.Instances.CALENDAR_ID}=$it)" }

            if (TextUtils.isEmpty(keyWord)) {/*
                selection = "(" + CalendarContract.Instances.VISIBLE + " = ?)" + " AND (" + categoryQuery + ")"
                selectionArgs = arrayOf("1")*/

                selection = "(" + CalendarContract.Instances.VISIBLE + " = ?)"
                selectionArgs = arrayOf("1")
            } else {
                selection = ("(" + CalendarContract.Instances.VISIBLE + " = ?) AND ((" +
                        CalendarContract.Instances.TITLE + " LIKE ?) OR (" + CalendarContract.Instances.DESCRIPTION + " LIKE ?))"
                        + " AND (" + categoryQuery + ")")
                selectionArgs = arrayOf("1", "%$keyWord%", "%$keyWord%")
            }

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endMillis)

            cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, CalendarContract.Instances.BEGIN + " asc")

            if (cur != null && cur.count > 0) {
                while (!cur.isLast) {
                    cur.moveToNext()
                    try {
                        instance_list.add(makeTimeObject(cur))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
            cur?.close()
        }

        return instance_list
    }

    val timeZone = TimeZone.getDefault().rawOffset

    private fun makeTimeObject(cur: Cursor) : TimeObject{
        val block = TimeObject(
                id = "osInstance::${cur.getLong(INDEX_ID)}",
                type = 0,
                title = cur.getString(INDEX_TITLE),
                color = cur.getInt(INDEX_EVENT_COLOR),
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