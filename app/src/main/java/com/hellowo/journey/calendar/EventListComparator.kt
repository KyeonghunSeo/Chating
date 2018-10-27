package com.hellowo.journey.calendar

import com.hellowo.journey.model.TimeObject

class EventListComparator : Comparator<TimeObject> {
    companion object {
        fun sort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.dtStart < r.dtStart -> -1
                l.dtStart > r.dtStart -> 1
                else -> l.title?.compareTo(r.title ?: "") ?: 1
            }
        }
    }

    override fun compare(l: TimeObject, r: TimeObject): Int {
        return sort(l, r)
    }
}