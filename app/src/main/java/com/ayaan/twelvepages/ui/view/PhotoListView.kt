package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.showDialog
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.bumptech.glide.Glide
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.list_item_photo.view.*

class PhotoListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var record: Record? = null
    val items = ArrayList<Link>()

    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = Adapter()
        isNestedScrollingEnabled = false
    }

    fun setList(to: Record) {
        items.clear()
        record = to
        items.addAll(to.links.filter { it.type == Link.Type.IMAGE.ordinal })
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
            val link = items[position]
            Glide.with(context).load(link.strParam0).into(v.imageView)
            v.imageView.setOnClickListener {
                ImageViewer.Builder(context, items.map { it.strParam0 })
                        .hideStatusBar(false)
                        .setStartPosition(items.indexOf(link))
                        .show()
            }
            v.imageView.setOnLongClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.delete_item), null, null) { result, _, _ ->
                    if(result) {
                        items.remove(link)
                        record?.links?.remove(link)
                        notifyItemRemoved(position)
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }
        }
    }
}