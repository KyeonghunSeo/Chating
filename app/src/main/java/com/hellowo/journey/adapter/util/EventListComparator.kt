package com.hellowo.journey.adapter.util

import com.hellowo.journey.model.TimeObject

class EventListComparator : Comparator<TimeObject> {
    companion object {
        fun sort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.dtStart < r.dtStart -> -1
                l.dtStart > r.dtStart -> 1
                else -> {
                    when{
                        l.ordering < r.ordering -> -1
                        l.ordering > r.ordering -> 1
                        else -> l.title?.compareTo(r.title ?: "") ?: 1
                    }
                }
            }
        }
    }

    override fun compare(l: TimeObject, r: TimeObject): Int {
        return sort(l, r)
    }
}