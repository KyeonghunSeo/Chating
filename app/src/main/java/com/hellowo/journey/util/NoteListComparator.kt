package com.hellowo.journey.util

import com.hellowo.journey.model.TimeObject

class NoteListComparator : Comparator<TimeObject> {
    companion object {
        fun sort(l: TimeObject, r: TimeObject): Int {
            return when{
                l.ordering < r.ordering -> -1
                l.ordering > r.ordering -> 1
                else -> {
                    when{
                        l.dtCreated < r.dtCreated -> -1
                        l.dtCreated > r.dtCreated -> 1
                        else -> 0
                    }
                }
            }
        }
    }

    override fun compare(l: TimeObject, r: TimeObject): Int {
        return sort(l, r)
    }
}