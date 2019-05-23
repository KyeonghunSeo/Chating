package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.showDialog
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.stfalcon.frescoimageviewer.ImageViewer

class LinkListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    var record: Record? = null

    init {
        orientation = VERTICAL
        //layoutTransition = LayoutTransition()
        //layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    fun setList(to: Record) {
        record = to
        removeAllViews()
        to.links.filter { it.type == Link.Type.IMAGE.ordinal }.forEach { makeItem(it) }
    }

    private fun makeItem(link: Link) {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_link, null, false)
        val imageView = v.findViewById<ImageView>(R.id.imageView)
        setGlobalTheme(v)
        Glide.with(context).load(link.properties).into(imageView)
        imageView.setOnClickListener {
            record?.links?.filter { it.type == Link.Type.IMAGE.ordinal }?.let { list ->
                ImageViewer.Builder(context, list.map { it.properties })
                        .hideStatusBar(false)
                        .setStartPosition(list.indexOf(link))
                        .show()
            }
        }

        imageView.setOnLongClickListener {
            showDialog(CustomDialog(context as Activity, context.getString(R.string.delete_item),
                    null, null) { result, _, _ ->
                if(result) {
                    record?.links?.remove(link)
                    removeView(v)
                }
            }, true, true, true, false)
            return@setOnLongClickListener true
        }
        addView(v)
    }

    private fun makeAddLy() {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_checklist_add, null, false)
        setGlobalTheme(v)
    }
}