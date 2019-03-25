package com.hellowo.journey.adapter.util

import com.hellowo.journey.model.TimeObject


class TimeObjectListComparator : Comparator<TimeObject> {
    companion object {
        private fun sort(l: TimeObject, r: TimeObject): Int {
            return  when {
                l.getFormula() < r.getFormula() -> -1
                l.getFormula() > r.getFormula() -> 1
                else -> {
                    when {
                        l.type < r.type -> -1
                        l.type > r.type -> 1
                        else -> {
                            when (l.type) {
                                TimeObject.Type.EVENT.ordinal -> {
                                    EventListComparator.sort(l, r)
                                }
                                TimeObject.Type.TASK.ordinal -> {
                                    TaskListComparator.sort(l, r)
                                }
                                TimeObject.Type.NOTE.ordinal -> {
                                    NoteListComparator.sort(l, r)
                                }
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