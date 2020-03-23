package com.ayaan.twelvepages.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Tag
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.list_item_tag.view.*

class TagAdapter(data: OrderedRealmCollection<Tag>, private val checkedItems: ArrayList<Tag>,
                 val adapterInterface: (action: Int, tag: Tag) -> Unit)
    : RealmRecyclerViewAdapter<Tag, TagAdapter.ViewHolder>(data, true) {

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tag, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        getItem(position)?.let { tag ->
            v.tagText.text = tag.id
            if(checkedItems.any{it.id == tag.id}) {
                v.tagText.setTextColor(AppTheme.secondaryText)
                v.tagText.typeface = AppTheme.boldFont
            }else {
                v.tagText.setTextColor(AppTheme.disableText)
                v.tagText.typeface = AppTheme.thinFont
            }
            v.deleteBtn.setOnClickListener { adapterInterface.invoke(1, tag) }
            v.setOnClickListener { adapterInterface.invoke(0, tag) }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.order?.toLong() ?: 0
    }
}