package com.hellowo.journey.manager

import android.app.Activity
import com.hellowo.journey.*
import com.hellowo.journey.model.KoreanLunarCalendar
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.ui.dialog.LunarRepeatDialog
import org.json.JSONObject
import java.lang.Exception
import java.util.*

object RepeatManager {
    private val instanceCal = Calendar.getInstance()
    private val dailyStr = "${App.context.getString(R.string.daily)} ${App.context.getString(R.string.repeat)}"
    private val dailyIntervalStr = App.context.getString(R.string.daily_interval)
    private val weeklyStr = "${App.context.getString(R.string.weekly)} %s ${App.context.getString(R.string.repeat)}"
    private val weeklyIntervalStr = App.context.getString(R.string.weekly_interval)
    private val monthlyStr = App.context.getString(R.string.monthly_interval)
    private val monthlyWStr = App.context.getString(R.string.monthly_w_interval)
    private val yearlyStr = "${App.context.getString(R.string.yearly)} %s ${App.context.getString(R.string.repeat)}"
    private val weekNumStr = App.context.getString(R.string.weekNum)
    private val untilStr = App.context.getString(R.string.until)
    private val lunarCal = KoreanLunarCalendar.getInstance()

    fun makeRepeatText(timeObject: TimeObject) : String {
        timeObject.repeat?.let {
            try {
                val repeatObject = JSONObject(it)
                return makeRepeatText(timeObject.dtStart,
                        repeatObject.getInt("freq"),
                        repeatObject.getInt("interval"),
                        repeatObject.getString("weekNum"),
                        repeatObject.getInt("monthOption"),
                        timeObject.dtUntil)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return App.context.getString(R.string.no_repeat)
    }

    fun makeRepeatText(time: Long, freq: Int, interval: Int, weekNum: String, monthOption: Int, dtUntil: Long) : String {
        instanceCal.timeInMillis = time
        val result = StringBuilder()
        when(freq) {
            0 -> {
                if(interval > 1) {
                    result.append(String.format(dailyIntervalStr, interval))
                }else {
                    result.append(dailyStr)
                }
            }
            1 -> {
                val dowBuilder = StringBuilder()

                if(weekNum != "0000000") {
                    var isFirst = true
                    weekNum.forEachIndexed { index, c ->
                        if(c == '1') {
                            if(isFirst) {
                                dowBuilder.append(AppDateFormat.dowString[index])
                                isFirst = false
                            }else {
                                dowBuilder.append(", ${AppDateFormat.dowString[index]}")
                            }
                        }
                    }
                }else {
                    dowBuilder.append(AppDateFormat.dow.format(instanceCal.time))
                }

                if(interval > 1) {
                    result.append(String.format(weeklyIntervalStr, interval, " $dowBuilder "))
                }else {
                    result.append(String.format(weeklyStr, dowBuilder))
                }
            }
            2 -> {
                if(monthOption == 0) {
                    result.append(String.format(monthlyStr, instanceCal.get(Calendar.DATE).toString()))
                }else {
                    result.append(String.format(monthlyWStr,
                            String.format(weekNumStr, instanceCal.get(Calendar.WEEK_OF_MONTH).toString()),
                            AppDateFormat.dow.format(instanceCal.time)))
                }
            }
            3 -> {
                result.append(String.format(yearlyStr, AppDateFormat.mdDate.format(instanceCal.time)))
            }
            4 -> {
                lunarCal.setSolarDate(instanceCal.get(Calendar.YEAR),
                        instanceCal.get(Calendar.MONTH) + 1,
                        instanceCal.get(Calendar.DATE))
                result.append("${lunarCal.lunarFormat} ${App.context.getString(R.string.repeat)}")
            }
        }

        if(dtUntil != Long.MIN_VALUE) {
            instanceCal.timeInMillis = dtUntil
            result.append(" - ${String.format(untilStr, AppDateFormat.ymdDate.format(instanceCal.time))}")
        }

        return result.toString()
    }

    fun makeRepeatInstance(timeObject: TimeObject, startTime: Long, end: Long) : ArrayList<TimeObject> {
        val result = ArrayList<TimeObject>()
        val repeatObject = JSONObject(timeObject.repeat)
        val freq = repeatObject.getInt("freq")
        val interval = repeatObject.getInt("interval")
        val weekNum = repeatObject.getString("weekNum")
        val monthOption = repeatObject.getInt("monthOption")
        val duration = timeObject.dtEnd - timeObject.dtStart
        val endTime = if(timeObject.dtUntil != Long.MIN_VALUE && timeObject.dtUntil < end && timeObject.dtUntil > startTime) {
            timeObject.dtUntil
        }else {
            end
        }

        instanceCal.timeInMillis = timeObject.dtStart

        if(freq == 4) {
            lunarCal.setSolarDate(instanceCal.get(Calendar.YEAR),
                    instanceCal.get(Calendar.MONTH) + 1,
                    instanceCal.get(Calendar.DATE))
        }

        while (instanceCal.timeInMillis <= endTime) {
            when(freq) {
                0 -> {
                    checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                    instanceCal.add(Calendar.DATE, interval)
                }
                1 -> {
                    if(weekNum != "0000000") {
                        weekNum.forEachIndexed { index, c ->
                            if(c == '1') {
                                instanceCal.set(Calendar.DAY_OF_WEEK, index + 1)
                                if(instanceCal.timeInMillis >= timeObject.dtStart) {
                                    checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                                }
                            }
                        }
                        instanceCal.set(Calendar.DAY_OF_WEEK, 1)
                    }else {
                        checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                    }
                    instanceCal.add(Calendar.DATE, 7 * interval)
                }
                2 -> {
                    checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                    val weekOfMonth = instanceCal.get(Calendar.WEEK_OF_MONTH)
                    val dayOfWeek = instanceCal.get(Calendar.DAY_OF_WEEK)
                    instanceCal.add(Calendar.MONTH, 1)
                    if(monthOption == 1) {
                        if(instanceCal.getActualMaximum(Calendar.WEEK_OF_MONTH) >= weekOfMonth) {
                            instanceCal.set(Calendar.WEEK_OF_MONTH, weekOfMonth)
                            instanceCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                        }else {
                            instanceCal.add(Calendar.MONTH, 1)
                        }
                    }
                }
                3 -> {
                    checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                    instanceCal.add(Calendar.YEAR, 1)
                }
                4 -> {
                    checkValidInstance(result, timeObject, instanceCal.timeInMillis, startTime, endTime, duration)
                    lunarCal.setLunarDate(lunarCal.lunarYear + 1, lunarCal.lunarMonth, lunarCal.lunarDay, lunarCal.isIntercalation)
                    instanceCal.set(lunarCal.solarYear, lunarCal.solarMonth - 1, lunarCal.solarDay)
                }
            }
        }

        return result
    }

    private fun checkValidInstance(result: ArrayList<TimeObject>, timeObject: TimeObject, instanceTime: Long,
                                   startTime: Long, endTime: Long, duration: Long) {
        val ymdKey = AppDateFormat.ymdkey.format(Date(instanceTime))
        if(instanceTime <= endTime && instanceTime + duration >= startTime && !timeObject.exDates.contains(ymdKey)) {
            result.add(makeInstance(timeObject, duration, ymdKey))
        }
    }

    private fun makeInstance(timeObject: TimeObject, duration: Long, ymdKey: String) : TimeObject {
        val instance = timeObject.makeCopyObject()
        instance.repeatKey = ymdKey
        instance.setDateTime(instance.allday, instanceCal.timeInMillis, instanceCal.timeInMillis + duration)
        return instance
    }

    fun save(activity: Activity, timeObject: TimeObject, runnable: Runnable) {
        val typeName = activity.getString(R.string.record)
        val title = String.format(activity.getString(R.string.repeat_save), typeName)
        val sub = activity.getString(R.string.how_apply)
        val options = arrayOf(
                String.format(activity.getString(R.string.apply_only), typeName),
                String.format(activity.getString(R.string.apply_after), typeName, typeName))
        showDialog(CustomDialog(activity, title, sub, options) { result, option, _ ->
            if(result) {
                when(option) {
                    0 -> {
                        TimeObjectManager.deleteOnly(timeObject)
                        timeObject.id = UUID.randomUUID().toString()
                        timeObject.clearRepeat()
                        TimeObjectManager.save(timeObject)
                    }
                    1 -> {
                        TimeObjectManager.deleteAfter(timeObject)
                        timeObject.id = UUID.randomUUID().toString()
                        TimeObjectManager.save(timeObject)
                    }
                }
                runnable.run()
            }
        }, true, true, true, false)
    }

    fun delete(activity: Activity, timeObject: TimeObject, runnable: Runnable) {
        val typeName = activity.getString(R.string.record)
        val title = String.format(activity.getString(R.string.repeat_delete), typeName)
        val sub = activity.getString(R.string.how_apply)
        val options = arrayOf(
                String.format(activity.getString(R.string.apply_only), typeName),
                String.format(activity.getString(R.string.apply_after), typeName, typeName),
                activity.getString(R.string.apply_all))
        showDialog(CustomDialog(activity, title, sub, options) { result, option, _ ->
            if(result) {
                when(option) {
                    0 -> TimeObjectManager.deleteOnly(timeObject)
                    1 -> TimeObjectManager.deleteAfter(timeObject)
                    2 -> TimeObjectManager.delete(timeObject)
                }
                runnable.run()
            }
        }, true, true, true, false)
    }

}