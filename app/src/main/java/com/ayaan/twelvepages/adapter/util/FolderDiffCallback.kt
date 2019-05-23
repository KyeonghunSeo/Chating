package com.ayaan.twelvepages.adapter.util

import androidx.recyclerview.widget.DiffUtil
import com.ayaan.twelvepages.model.Folder

class FolderDiffCallback(private val oldList: List<Folder>, private val newList: List<Folder>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldList[oldItemPosition] == newList[newItemPosition]
}