package com.hellowo.journey.adapter.util

import androidx.recyclerview.widget.DiffUtil
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject

class TemplateDiffCallback(private val oldList: List<Template>, private val newList: List<Template>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition] == newList[newItemPosition]
}