package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.list_item_link.view.*

class WebLinkListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var record: Record? = null
    val items = ArrayList<Link>()

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter = Adapter()
        isNestedScrollingEnabled = false
    }

    fun setList(to: Record) {
        items.clear()
        record = to
        items.addAll(to.links.filter { it.type == Link.Type.WEB.ordinal })
        adapter?.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init { setGlobalTheme(container) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_link, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = (holder as ViewHolder).itemView
            val link = items[position]
            val url = link.strParam0
            val imageurl = link.strParam1
            val favicon = link.strParam2

            v.linkText.text = link.title
            if(!imageurl.isNullOrBlank()){
                Glide.with(context).asBitmap().load(imageurl).into(object : SimpleTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if(resource.width > dpToPx(80) || resource.height > dpToPx(40)) {
                            (v.linkImg.layoutParams as LinearLayout.LayoutParams).let {
                                it.width = dpToPx(80)
                                it.height = dpToPx(40)
                            }
                            v.linkImg.setPadding(0, 0, 0, 0)
                            v.linkImg.requestLayout()
                        }else {
                            (v.linkImg.layoutParams as LinearLayout.LayoutParams).let {
                                it.width = dpToPx(40)
                                it.height = dpToPx(40)
                            }
                            val p = dpToPx(10)
                            v.linkImg.setPadding(p, p, p, p)
                            v.linkImg.requestLayout()
                        }
                        v.linkImg.setImageBitmap(resource)
                    }
                })
            } else if(!favicon.isNullOrBlank()) {
                Glide.with(context).load(favicon).into(v.linkImg)
            } else {
                Glide.with(context).load(R.drawable.website).into(v.linkImg)
            }

            v.linkImg.setOnClickListener {
                try{
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }catch (e: Exception) {
                    toast(R.string.invalid_info)
                }
            }
            v.linkImg.setOnLongClickListener {
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