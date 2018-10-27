package com.hellowo.journey.calendar

import com.hellowo.journey.adapter.TimeObjectCalendarAdapter
import com.hellowo.journey.model.TimeObject

class CalendarComparator : Comparator<TimeObjectCalendarAdapter.TimeObjectViewHolder> {
    override fun compare(l: TimeObjectCalendarAdapter.TimeObjectViewHolder, r: TimeObjectCalendarAdapter.TimeObjectViewHolder): Int {
        return when{
            l.timeObject.getFormula() < r.timeObject.getFormula() -> -1
            l.timeObject.getFormula() > r.timeObject.getFormula() -> 1
            else -> {
                when{
                    l.timeObject.type < r.timeObject.type -> -1
                    l.timeObject.type > r.timeObject.type -> 1
                    else -> {
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
                                        when(l.timeObject.type) {
                                            TimeObject.Type.EVENT.ordinal -> {
                                                EventListComparator.sort(l.timeObject, r.timeObject)
                                            }
                                            TimeObject.Type.TASK.ordinal -> {
                                                TaskListComparator.sort(l.timeObject, r.timeObject)
                                            }
                                            else -> {
                                                l.timeObject.title?.compareTo(r.timeObject.title ?: "") ?: 1
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