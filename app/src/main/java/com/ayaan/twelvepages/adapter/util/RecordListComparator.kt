package com.ayaan.twelvepages.adapter.util

import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.model.Record


class RecordListComparator : Comparator<Record> {
    companion object {
        private fun sort(l: Record, r: Record): Int {
            return when{
                l.ordering < r.ordering -> -1
                l.ordering > r.ordering -> 1
                else -> when {
                    l.isScheduled() && r.isScheduled() -> {
                        val lf = l.getFormula()
                        val rf = r.getFormula()
                        when {
                            lf < rf -> -1
                            lf > rf -> 1
                            else -> {
                                if (lf == RecordCalendarAdapter.Formula.RANGE) {
                                    when{
                                        l.getDuration() < r.getDuration() -> -1
                                        l.getDuration() > r.getDuration() -> 1
                                        else -> {
                                            when{
                                                l.dtStart < r.dtStart -> -1
                                                l.dtStart > r.dtStart -> 1
                                                else -> lastSort(l, r)
                                            }
                                        }
                                    }
                                }else {
                                    when{
                                        l.getDuration() > r.getDuration() -> -1
                                        l.getDuration() > r.getDuration() -> 1
                                        else -> {
                                            when{
                                                l.dtStart < r.dtStart -> -1
                                                l.dtStart > r.dtStart -> 1
                                                else -> lastSort(l, r)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> lastSort(l, r)
                }
            }
        }

        fun lastSort(l: Record, r: Record): Int {
            return when {
                l.dtCreated < r.dtCreated -> -1
                l.dtCreated > r.dtCreated -> 1
                else -> l.title?.compareTo(r.title ?: "") ?: 1
            }
        }
    }

    override fun compare(l: Record, r: Record): Int {
        return sort(l, r)
    }
}