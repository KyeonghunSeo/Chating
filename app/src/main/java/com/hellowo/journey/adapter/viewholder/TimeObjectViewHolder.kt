package com.hellowo.journey.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hellowo.journey.R
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.setGlobalTheme
import kotlinx.android.synthetic.main.list_item_event.view.*
import kotlinx.android.synthetic.main.view_timeobject_sub_contents.view.*
import org.json.JSONObject

class TimeObjectViewHolder(container: View) : RecyclerView.ViewHolder(container) {
    init {
        setGlobalTheme(container)
    }

    fun onItemSelected() {
        //itemView.setBackgroundColor(Color.LTGRAY)
    }

    fun onItemClear() {
        //itemView.setBackgroundColor(0)
    }

    @SuppressLint("SetTextI18n")
    fun setContents(context: Context, timeObject: TimeObject, v: View,
                    adapterInterface: (view: View, timeObject: TimeObject, action: Int) -> Unit) {
        if(timeObject.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
        }else {
            v.tagText.visibility = View.GONE
        }

        if(timeObject.title.isNullOrBlank()) {
            v.titleText.text = context.getString(R.string.untitle)
        }else {
            v.titleText.text = timeObject.title?.trim()
        }

        if(timeObject.description.isNullOrBlank()) {
            v.memoLy.visibility = View.GONE
        }else {
            v.memoLy.visibility = View.VISIBLE
            v.memoText.text = timeObject.description
        }

        if(timeObject.location.isNullOrBlank()) {
            v.locationLy.visibility = View.GONE
        }else {
            v.locationLy.visibility = View.VISIBLE
            v.locationText.text = timeObject.location?.replace("\n", " - ")
        }

        if(timeObject.alarms.isNotEmpty()) {
            v.alarmText.text = timeObject.alarms.joinToString(", ") {
                AlarmManager.getTimeObjectAlarmText(context, it) }
            v.alarmLy.visibility = View.VISIBLE
        }else {
            v.alarmLy.visibility = View.GONE
        }

        if(!timeObject.repeat.isNullOrBlank()) {
            v.repeatLy.visibility = View.VISIBLE
            v.repeatText.text = RepeatManager.makeRepeatText(timeObject)
        }else {
            v.repeatLy.visibility = View.GONE
        }

        if(timeObject.links.any { it.type == Link.Type.IMAGE.ordinal }){
            val list = timeObject.links.filter{ it.type == Link.Type.IMAGE.ordinal }

            Glide.with(context).load(list[0].properties).into(v.mainImgView)

            if(list.size > 1) {
                v.subImageLy.visibility = View.VISIBLE
                Glide.with(context).load(list[1].properties).into(v.subImageView)
                if(list.size > 2) {
                    v.subImageText.text = "+${list.size - 2}"
                    v.subImageText.visibility = View.VISIBLE
                }else {
                    v.subImageText.visibility = View.GONE
                }
            }
            else v.subImageLy.visibility = View.GONE

            v.imageLy.visibility = View.VISIBLE
        }else {
            v.imageLy.visibility = View.GONE
        }

        if(timeObject.links.any { it.type == Link.Type.WEB.ordinal }){
            val link = timeObject.links.first{ it.type == Link.Type.WEB.ordinal }
            val properties = JSONObject(link.properties)
            val url = properties.getString("url")
            val imageurl = properties.getString("imageurl")
            val favicon = properties.getString("favicon")

            v.linkText.text = link.title
            if(!imageurl.isNullOrBlank())
                Glide.with(context).load(imageurl).into(v.linkImg)
            else if(!favicon.isNullOrBlank())
                Glide.with(context).load(favicon).into(v.linkImg)
            else {
                Glide.with(context).load(R.drawable.sharp_language_black_48dp).into(v.linkImg)
            }

            v.linkLy.visibility = View.VISIBLE
            v.linkLy.setOnClickListener {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }else {
            v.linkLy.visibility = View.GONE
        }
    }
}