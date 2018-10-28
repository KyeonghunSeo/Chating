package com.hellowo.journey.calendar

import com.hellowo.journey.model.TimeObject

class TaskListComparator : Comparator<TimeObject> {
    companion object {
        fun sort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.dtDone < r.dtDone -> -1
                l.dtDone > r.dtDone -> 1
                else -> {
                    when{
                        l.ordering < r.ordering -> -1
                        l.ordering > r.ordering -> 1
                        else -> {
                            when{
                                l.dtCreated < r.dtCreated -> 1
                                l.dtCreated > r.dtCreated -> -1
                                else -> {
                                    l.title?.compareTo(r.title ?: "") ?: 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun compare(l: TimeObject, r: TimeObject): Int {
        return sort(l, r)
    }
}