package com.hellowo.journey.ui.view

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hellowo.journey.R
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.vibrate
import com.stfalcon.frescoimageviewer.ImageViewer
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class LinkListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    var timeObject: TimeObject? = null

    init {
        orientation = VERTICAL
        //layoutTransition = LayoutTransition()
        //layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    fun setList(to: TimeObject) {
        timeObject = to
        removeAllViews()
        to.links.filter { it.type == Link.Type.IMAGE.ordinal }.forEach { makeItem(it) }
    }

    private fun makeItem(link: Link) {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_link, null, false)
        val imageView = v.findViewById<ImageView>(R.id.imageView)
        setGlobalTheme(v)
        Glide.with(context).load(link.properties).into(imageView)
        imageView.setOnClickListener {
            timeObject?.links?.filter { it.type == Link.Type.IMAGE.ordinal }?.let { list ->
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
                    timeObject?.links?.remove(link)
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