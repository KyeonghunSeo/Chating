package com.ayaan.twelvepages.adapter.util

import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.model.Record

class RecordCalendarComparator : Comparator<RecordCalendarAdapter.TimeObjectViewHolder> {
    override fun compare(l: RecordCalendarAdapter.TimeObjectViewHolder, r: RecordCalendarAdapter.TimeObjectViewHolder): Int {
        return when{
            l.formula < r.formula -> -1
            l.formula > r.formula -> 1
            else -> {
                if(l.formula == Record.Formula.RANGE) {
                    when{
                        l.startCellNum > r.startCellNum -> -1
                        l.startCellNum < r.startCellNum -> 1
                        else -> {
                            val lLength = l.endCellNum - l.startCellNum
                            val rLength = r.endCellNum - r.startCellNum
                            when{
                                lLength < rLength -> -1
                                lLength > rLength -> 1
                                else -> RecordListComparator.sort(l.record, r.record)
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
                                else -> RecordListComparator.sort(l.record, r.record)
                            }
                        }
                    }
                }
            }
        }
    }
}