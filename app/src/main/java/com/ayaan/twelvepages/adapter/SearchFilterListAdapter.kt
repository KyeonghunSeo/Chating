package com.ayaan.twelvepages.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.AppDateFormat
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.SearchFilter
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.str
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_search_filter.view.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class SearchFilterListAdapter(private val context: Context, private val items: ArrayList<SearchFilter>,
                              private val adapterInterface: (view: View, record: SearchFilter, filter: JSONObject, action: Int) -> Unit)
    : RecyclerView.Adapter<SearchFilterListAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_search_filter, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val v = holder.itemView
        val filter = JSONObject(item.filter)
        try{
            val keyword = filter.getString("keyword")
            val startTime = filter.getLong("startTime")
            val endTime = filter.getLong("endTime")
            val tagTitles = filter.getString("tagTitles")
            val colorKey = filter.getInt("colorKey")
            val isCheckBox = filter.getBoolean("isCheckBox")
            val isPhoto = filter.getBoolean("isPhoto")

            if(keyword.isNotEmpty()) {
                v.titleText.text = keyword
            }else {
                v.titleText.text = str(R.string.no_keyword)
            }

            if(startTime != Long.MIN_VALUE) {
                v.dateText.text = "${AppDateFormat.simpleYmdDate.format(Date(startTime))} ~ ${AppDateFormat.simpleYmdDate.format(Date(endTime))}"
            }else {
                v.dateText.text = str(R.string.whole_range)
            }

            if(tagTitles.isNotEmpty()) {
                v.tagText.visibility = View.VISIBLE
                v.tagText.text = tagTitles.split("||").joinToString("#", prefix = "#")
            }else {
                v.tagText.visibility = View.GONE
            }

            if(colorKey != Int.MIN_VALUE) {
                v.colorImg.visibility = View.VISIBLE
                v.colorImg.setColorFilter(ColorManager.getColor(colorKey))
            }else {
                v.colorImg.visibility = View.GONE
            }

            if(isCheckBox) {
                v.checkImg.visibility = View.VISIBLE
            }else {
                v.checkImg.visibility = View.GONE
            }

            if(isPhoto) {
                v.photoImg.visibility = View.VISIBLE
            }else {
                v.photoImg.visibility = View.GONE
            }

            v.deleteBtn.setOnClickListener {
                val pos = items.indexOf(item)
                items.remove(item)
                notifyItemRemoved(pos)
                adapterInterface.invoke(v, item, filter, -1)
            }
            v.setOnClickListener { adapterInterface.invoke(v, item, filter, 0) }
        }catch (e: Exception){ e.printStackTrace() }
    }
}