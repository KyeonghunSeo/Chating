package com.ayaan.twelvepages.adapter.util

import com.ayaan.twelvepages.adapter.RecordCalendarAdapter

class RecordCalendarComparator : Comparator<RecordCalendarAdapter.RecordViewHolder> {
    override fun compare(l: RecordCalendarAdapter.RecordViewHolder, r: RecordCalendarAdapter.RecordViewHolder): Int {
        return when{
            l.formula.order < r.formula.order -> -1
            l.formula.order > r.formula.order -> 1
            else -> {
                if(l.formula == RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT) {
                    when{
                        l.startCellNum > r.startCellNum -> -1
                        l.startCellNum < r.startCellNum -> 1
                        else -> {
                            val lLength = l.endCellNum - l.startCellNum
                            val rLength = r.endCellNum - r.startCellNum
                            when{
                                lLength < rLength -> -1
                                lLength > rLength -> 1
                                else -> {
                                    val lr = l.record
                                    val rr = r.record
                                    when{
                                        lr.dtStart < rr.dtStart -> -1
                                        lr.dtStart > rr.dtStart -> 1
                                        else -> RecordListComparator.lastSort(lr, rr)
                                    }
                                }
                            }
                        }
                    }
                }else {
                    when{
                        l.startCellNum < r.startCellNum -> -1
                        l.startCellNum > r.startCellNum -> 1
                        else -> {
                            val lLength = l.endCellNum - l.startCellNum
                            val rLength = r.endCellNum - r.startCellNum
                            when{
                                lLength > rLength -> -1
                                lLength < rLength -> 1
                                else -> {
                                    val lr = l.record
                                    val rr = r.record
                                    when{
                                        lr.ordering < rr.ordering -> -1
                                        lr.ordering > rr.ordering -> 1
                                        else -> {
                                            when{
                                                lr.dtStart < rr.dtStart -> -1
                                                lr.dtStart > rr.dtStart -> 1
                                                else -> RecordListComparator.lastSort(lr, rr)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}