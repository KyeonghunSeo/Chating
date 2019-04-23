package com.hellowo.journey.adapter.util

import com.hellowo.journey.model.Record


class RecordListComparator : Comparator<Record> {
    companion object {
        fun sort(l: Record, r: Record): Int {
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

        private fun lastSort(l: Record, r: Record): Int {
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

    override fun compare(l: Record, r: Record): Int {
        return sort(l, r)
    }
}