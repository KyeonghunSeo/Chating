package com.ayaan.twelvepages

import android.annotation.SuppressLint
import android.content.Context
import com.ayaan.twelvepages.App.Companion.resource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object AppDateFormat {
    enum class HourMode { Hour24, Hour12 }
    lateinit var dows: Array<String>
    lateinit var dowEngs: Array<String>
    lateinit var hourMode: HourMode
    lateinit var ymde: DateFormat
    lateinit var ymd: DateFormat
    lateinit var mde: DateFormat
    lateinit var mdeShort: DateFormat
    lateinit var md: DateFormat
    lateinit var month: DateFormat
    lateinit var ym: DateFormat
    lateinit var time: DateFormat
    lateinit var dateTime: DateFormat
    lateinit var dow: DateFormat
    lateinit var simpleDow: DateFormat
    lateinit var date: DateFormat
    lateinit var hour: DateFormat
    lateinit var year: DateFormat
    val monthEng = SimpleDateFormat("MMMM", Locale.ENGLISH)
    val dowEng = SimpleDateFormat("EEE", Locale.ENGLISH)
    val dowfullEng = SimpleDateFormat("EEEE", Locale.ENGLISH)
    val ymSimpleDate = SimpleDateFormat("yyyy.M")
    val ymdkey = SimpleDateFormat("yyyyMMdd")
    val simpleYmdDate = SimpleDateFormat("yy.MM.dd")
    val ymdthmszkey: DateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

    @SuppressLint("ConstantLocale")
    val language = Locale.getDefault().language

    fun init(context: Context) {
        //////////////////////////////////////////////////////////////////////////날짜 포맷

        val mDateFormat = android.text.format.DateFormat.getDateFormat(context)
        val mTimeFormat = android.text.format.DateFormat.getTimeFormat(context)

        val cal = Calendar.getInstance()
        cal.set(1982, 10, 22)

        val date_str = mDateFormat.format(cal.time)
        val time_str = mTimeFormat.format(cal.time)

        if (date_str != null && time_str != null) {
            val y_pos = date_str.indexOf("1982")
            val m_pos = date_str.indexOf("11")
            val d_pos = date_str.indexOf("22")

            if (m_pos < y_pos && m_pos < d_pos) {
                when (language) {
                    "ko" -> {
                        ymde = SimpleDateFormat("M월 d일 yyyy년 E요일")
                        mde = SimpleDateFormat("M월 d일 E요일")
                        mdeShort = SimpleDateFormat("M월 d일 E")
                        md = SimpleDateFormat("M월 d일")
                        ymd = SimpleDateFormat("M월 d일 yyyy년")
                        ym = SimpleDateFormat("M월 yyyy년")
                    }
                    "ja" -> {
                        ymde = SimpleDateFormat("EEEE, MMMM d日, yyyy")
                        mde = SimpleDateFormat("EEE, MMM d日")
                        mdeShort = SimpleDateFormat("EEE, MMM d日")
                        md = SimpleDateFormat("MMM d日")
                        ymd = SimpleDateFormat("MMM d日, yyyy")
                        ym = SimpleDateFormat("MMM, yyyy")
                    }
                    else -> {
                        ymde = SimpleDateFormat("EEEE, MMMM d, yyyy")
                        mde = SimpleDateFormat("EEE, MMM d")
                        mdeShort = SimpleDateFormat("EEE, MMM d")
                        md = SimpleDateFormat("MMM d")
                        ymd = SimpleDateFormat("MMM d, yyyy")
                        ym = SimpleDateFormat("MMM, yyyy")
                    }
                }
            } else if (m_pos in (d_pos + 1)..(y_pos - 1)) {
                when (language) {
                    "ko" -> {
                        ymde = SimpleDateFormat("d일 M월 yyyy년 E요일")
                        mde = SimpleDateFormat("d일 M월 E요일")
                        mdeShort = SimpleDateFormat("d일 M월 E")
                        md = SimpleDateFormat("d일 M월")
                        ymd = SimpleDateFormat("d일 M월 yyyy년")
                        ym = SimpleDateFormat("M월 yyyy년")
                    }
                    "ja" -> {
                        ymde = SimpleDateFormat("EEEE, d日 MMMM, yyyy")
                        mde = SimpleDateFormat("EEE, d日 MMM")
                        mdeShort = SimpleDateFormat("E, d日 MMM")
                        md = SimpleDateFormat("d日 MMM")
                        ymd = SimpleDateFormat("d日 MMM, yyyy")
                        ym = SimpleDateFormat("MMM, yyyy")
                    }
                    else -> {
                        ymde = SimpleDateFormat("EEEE, d MMMM, yyyy")
                        mde = SimpleDateFormat("EEE, d MMM")
                        mdeShort = SimpleDateFormat("E, d MMM")
                        md = SimpleDateFormat("d MMM")
                        ymd = SimpleDateFormat("d MMM, yyyy")
                        ym = SimpleDateFormat("MMM, yyyy")
                    }
                }
            } else {
                when (language) {
                    "ko" -> {
                        ymde = SimpleDateFormat("yyyy년 M월 d일 E요일")
                        mde = SimpleDateFormat("M월 d일 E요일")
                        mdeShort = SimpleDateFormat("M월 d일 E")
                        md = SimpleDateFormat("M월 d일")
                        ymd = SimpleDateFormat("yyyy년 M월 d일")
                        ym = SimpleDateFormat("yyyy년 M월")
                    }
                    "ja" -> {
                        ymde = SimpleDateFormat("EEEE, yyyy, MMMM d日")
                        mde = SimpleDateFormat("EEE, MMM d日")
                        mdeShort = SimpleDateFormat("E, MMM d日")
                        md = SimpleDateFormat("MMM d日")
                        ymd = SimpleDateFormat("yyyy, MMM d日")
                        ym = SimpleDateFormat("yyyy, MMM")
                    }
                    else -> {
                        ymde = SimpleDateFormat("EEEE, yyyy, MMMM d")
                        mde = SimpleDateFormat("EEE, MMM d")
                        mdeShort = SimpleDateFormat("E, MMM d")
                        md = SimpleDateFormat("MMM d")
                        ymd = SimpleDateFormat("yyyy, MMM d")
                        ym = SimpleDateFormat("yyyy, MMM")
                    }
                }
            }

            year = if (language == "ko") {
                SimpleDateFormat("yyyy년")
            } else {
                SimpleDateFormat("yyyy")
            }

            month = if (language == "ko") {
                SimpleDateFormat("MMM")
            } else {
                SimpleDateFormat("MMM")
            }

            date = when (language) {
                "ko" -> SimpleDateFormat("d일")
                "ja" -> SimpleDateFormat("d日")
                else -> SimpleDateFormat("d")
            }

            dows = resource.getStringArray(R.array.day_of_weeks)
            dowEngs = resource.getStringArray(R.array.day_of_weeks_eng)
            simpleDow = if (language == "ko") {
                SimpleDateFormat("E")
            } else {
                SimpleDateFormat("E")
            }

            dow = if (language == "ko") {
                SimpleDateFormat("E요일")
            } else {
                SimpleDateFormat("EEEE")
            }

            ymdthmszkey.timeZone = TimeZone.getTimeZone("GMT")

            if (time_str.length > 5) {
                hourMode = HourMode.Hour12
                time = SimpleDateFormat("a hh:mm")
                if (language == "ko") {
                    dateTime = SimpleDateFormat("d일 a h:mm")
                    hour = SimpleDateFormat("a h시")
                } else {
                    dateTime = SimpleDateFormat("d, h:mm a")
                    hour = SimpleDateFormat("a h")
                }
            } else {
                hourMode = HourMode.Hour24
                time = SimpleDateFormat("HH:mm")
                if (language == "ko") {
                    dateTime = SimpleDateFormat("d일 H:mm")
                    hour = SimpleDateFormat("H시")
                } else {
                    dateTime = SimpleDateFormat("d, H:mm")
                    hour = SimpleDateFormat("H")
                }
            }
        }
    }
}