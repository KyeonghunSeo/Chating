package com.hellowo.journey.adapter.util

import com.hellowo.journey.model.TimeObject


class TimeObjectListComparator : Comparator<TimeObject> {
    companion object {
        fun sort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.ordering < r.ordering -> -1
                l.ordering > r.ordering -> 1
                else -> when {
                    l.isScheduled() && r.isScheduled() -> {
                        when{
                            l.dtStart < r.dtStart -> -1
                            l.dtStart > r.dtStart -> 1
                            else -> lastSort(l, r)
                        }
                    }
                    else -> lastSort(l, r)
                }
            }
        }

        private fun lastSort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.getDuration() < r.getDuration() -> 1
                l.getDuration() > r.getDuration() -> -1
                else -> {
                    when {
                        l.dtCreated < r.dtCreated -> -1
                        l.dtCreated > r.dtCreated -> 1
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