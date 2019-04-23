package com.hellowo.journey.adapter.util

import androidx.recyclerview.widget.DiffUtil
import com.hellowo.journey.model.Record

class ListDiffCallback(private val oldList: List<Record>, private val newList: List<Record>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition] == newList[newItemPosition]

    /* 세로선 이어주는 로직
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition] == newList[newItemPosition] &&
            !((oldItemPosition == 0 && newItemPosition != 0 || oldItemPosition != 0 && newItemPosition == 0)
             || (oldItemPosition == oldList.size - 1 && newItemPosition != newList.size - 1
                    || oldItemPosition != oldList.size - 1 && newItemPosition == newList.size - 1))
                    */
}