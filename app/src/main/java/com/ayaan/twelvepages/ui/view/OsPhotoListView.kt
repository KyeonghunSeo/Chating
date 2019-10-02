package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Photo
import com.ayaan.twelvepages.setGlobalTheme
import com.bumptech.glide.Glide
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.list_item_photo.view.*

class OsPhotoListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    val items = ArrayList<Photo>()

    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = Adapter()
        isNestedScrollingEnabled = false
    }

    fun setList(to: ArrayList<Photo>) {
        items.clear()
        items.addAll(to)
        adapter?.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init { setGlobalTheme(container) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_photo, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = (holder as ViewHolder).itemView
            val photo = items[position]
            Glide.with(context).load(photo.url).into(v.imageView)
            v.imageView.setOnClickListener {
                ImageViewer.Builder(context, items.map { "file://${it.url}" })
                        .hideStatusBar(false)
                        .setStartPosition(position)
                        .show()
            }
        }
    }
}