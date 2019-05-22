package com.hellowo.journey.adapter.util

import com.hellowo.journey.adapter.RecordCalendarAdapter
import com.hellowo.journey.model.Record

class RecordCalendarComparator : Comparator<RecordCalendarAdapter.TimeObjectViewHolder> {
    override fun compare(l: RecordCalendarAdapter.TimeObjectViewHolder, r: RecordCalendarAdapter.TimeObjectViewHolder): Int {
        return when{
            l.record.getFormula() < r.record.getFormula() -> -1
            l.record.getFormula() > r.record.getFormula() -> 1
            else -> {
                if(l.record.getFormula() == Record.Formula.SINGLE_LINE_BOTTOM_STACK) {
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