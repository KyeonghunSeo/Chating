package com.hellowo.journey.adapter.util

import com.hellowo.journey.adapter.TimeObjectCalendarAdapter
import com.hellowo.journey.model.TimeObject

class CalendarComparator : Comparator<TimeObjectCalendarAdapter.TimeObjectViewHolder> {
    override fun compare(l: TimeObjectCalendarAdapter.TimeObjectViewHolder, r: TimeObjectCalendarAdapter.TimeObjectViewHolder): Int {
        return when{
            l.timeObject.getFormula() < r.timeObject.getFormula() -> -1
            l.timeObject.getFormula() > r.timeObject.getFormula() -> 1
            else -> {
                if(l.timeObject.getFormula() == TimeObject.Formula.BOTTOM_STACK) {
                    when{
                        l.startCellNum > r.startCellNum -> -1
                        l.startCellNum < r.startCellNum -> 1
                        else -> {
                            val lLength = l.endCellNum - l.startCellNum
                            val rLength = r.endCellNum - r.startCellNum
                            when{
                                lLength < rLength -> -1
                                lLength > rLength -> 1
                                else -> TimeObjectListComparator.sort(l.timeObject, r.timeObject)
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
                                else -> TimeObjectListComparator.sort(l.timeObject, r.timeObject)
                            }
                        }
                    }
                }
            }
        }
    }
}