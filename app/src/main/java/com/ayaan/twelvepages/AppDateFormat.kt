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
    lateinit var dowString: Array<String>
    lateinit var dowEngString: Array<String>
    lateinit var hourMode: HourMode
    lateinit var ymdeDate: DateFormat
    lateinit var ymdDate: DateFormat
    lateinit var mdeDate: DateFormat
    lateinit var mdeShortDate: DateFormat
    lateinit var mdDate: DateFormat
    lateinit var mDate: DateFormat
    lateinit var ymDate: DateFormat
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
                        ymdeDate = SimpleDateFormat("M월 d일 yyyy년 E요일")
                        mdeDate = SimpleDateFormat("M월 d일 E요일")
                        mdeShortDate = SimpleDateFormat("M월 d일 E")
                        mdDate = SimpleDateFormat("M월 d일")
                        ymdDate = SimpleDateFormat("M월 d일 yyyy년")
                        ymDate = SimpleDateFormat("M월 yyyy년")
                    }
                    "ja" -> {
                        ymdeDate = SimpleDateFormat("EEEE, MMMM d日, yyyy")
                        mdeDate = SimpleDateFormat("EEE, MMM d日")
                        mdeShortDate = SimpleDateFormat("EEE, MMM d日")
                        mdDate = SimpleDateFormat("MMM d日")
                        ymdDate = SimpleDateFormat("MMM d日, yyyy")
                        ymDate = SimpleDateFormat("MMM, yyyy")
                    }
                    else -> {
                        ymdeDate = SimpleDateFormat("EEEE, MMMM d, yyyy")
                        mdeDate = SimpleDateFormat("EEE, MMM d")
                        mdeShortDate = SimpleDateFormat("EEE, MMM d")
                        mdDate = SimpleDateFormat("MMM d")
                        ymdDate = SimpleDateFormat("MMM d, yyyy")
                        ymDate = SimpleDateFormat("MMM, yyyy")
                    }
                }
            } else if (m_pos in (d_pos + 1)..(y_pos - 1)) {
                when (language) {
                    "ko" -> {
                        ymdeDate = SimpleDateFormat("d일 M월 yyyy년 E요일")
                        mdeDate = SimpleDateFormat("d일 M월 E요일")
                        mdeShortDate = SimpleDateFormat("d일 M월 E")
                        mdDate = SimpleDateFormat("d일 M월")
                        ymdDate = SimpleDateFormat("d일 M월 yyyy년")
                        ymDate = SimpleDateFormat("M월 yyyy년")
                    }
                    "ja" -> {
                        ymdeDate = SimpleDateFormat("EEEE, d日 MMMM, yyyy")
                        mdeDate = SimpleDateFormat("EEE, d日 MMM")
                        mdeShortDate = SimpleDateFormat("EEE, d日 MMM")
                        mdDate = SimpleDateFormat("d日 MMM")
                        ymdDate = SimpleDateFormat("d日 MMM, yyyy")
                        ymDate = SimpleDateFormat("MMM, yyyy")
                    }
                    else -> {
                        ymdeDate = SimpleDateFormat("EEEE, d MMMM, yyyy")
                        mdeDate = SimpleDateFormat("EEE, d MMM")
                        mdeShortDate = SimpleDateFormat("EEE, d MMM")
                        mdDate = SimpleDateFormat("d MMM")
                        ymdDate = SimpleDateFormat("d MMM, yyyy")
                        ymDate = SimpleDateFormat("MMM, yyyy")
                    }
                }
            } else {
                when (language) {
                    "ko" -> {
                        ymdeDate = SimpleDateFormat("yyyy년 M월 d일 E요일")
                        mdeDate = SimpleDateFormat("M월 d일 E요일")
                        mdeShortDate = SimpleDateFormat("M월 d일 E")
                        mdDate = SimpleDateFormat("M월 d일")
                        ymdDate = SimpleDateFormat("yyyy년 M월 d일")
                        ymDate = SimpleDateFormat("yyyy년 M월")
                    }
                    "ja" -> {
                        ymdeDate = SimpleDateFormat("EEEE, yyyy, MMMM d日")
                        mdeDate = SimpleDateFormat("EEE, MMM d日")
                        mdeShortDate = SimpleDateFormat("EEE, MMM d日")
                        mdDate = SimpleDateFormat("MMM d日")
                        ymdDate = SimpleDateFormat("yyyy, MMM d日")
                        ymDate = SimpleDateFormat("yyyy, MMM")
                    }
                    else -> {
                        ymdeDate = SimpleDateFormat("EEEE, yyyy, MMMM d")
                        mdeDate = SimpleDateFormat("EEE, MMM d")
                        mdeShortDate = SimpleDateFormat("EEE, MMM d")
                        mdDate = SimpleDateFormat("MMM d")
                        ymdDate = SimpleDateFormat("yyyy, MMM d")
                        ymDate = SimpleDateFormat("yyyy, MMM")
                    }
                }
            }

            date = when (language) {
                "ko" -> SimpleDateFormat("d일")
                "ja" -> SimpleDateFormat("d日")
                else -> SimpleDateFormat("d")
            }

            year = SimpleDateFormat("yyyy")

            mDate = if (language == "en") {
                SimpleDateFormat("MMM")
            } else {
                SimpleDateFormat("MMM")
            }

            dowString = resource.getStringArray(R.array.day_of_weeks)
            dowEngString = resource.getStringArray(R.array.day_of_weeks_eng)
            simpleDow = if (language == "ko") {
                SimpleDateFormat("E요일")
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