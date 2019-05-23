package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.showDialog
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.vibrate
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CheckListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    val items = ArrayList<JSONObject>()
    var record: Record? = null
    var jsonArray: JSONArray? = null

    init {
        orientation = VERTICAL
        //layoutTransition = LayoutTransition()
        //layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    fun setCheckList(to: Record) {
        record = to
        items.clear()
        jsonArray = JSONArray()
        removeAllViews()
        makeAddLy()
        to.links.first { it.type == Link.Type.CHECKLIST.ordinal }?.let { link ->
            try{
                JSONArray(link.properties).let {
                    if(it.length() > 0) {
                        for(i in 0 until it.length()) {
                            makeItem(it.getJSONObject(i))
                        }
                    }
                }
                return@let
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun makeItem(item: JSONObject) {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_checklist, null, false)
        val iconImg = v.findViewById<ImageView>(R.id.iconImg)
        val titleText = v.findViewById<TextView>(R.id.titleText)
        setGlobalTheme(v)

        iconImg.setOnClickListener {
            vibrate(context)
            if(item.getLong("dtDone") == Long.MIN_VALUE) {
                item.put("dtDone", System.currentTimeMillis())
            }else {
                item.put("dtDone", Long.MIN_VALUE)
            }
            setItem(v, item)
            save()
        }

        titleText.setOnClickListener {
            val dialog = CustomDialog(context as Activity, context.getString(R.string.edit_item),
                    null, null) { result, _, text ->
                if(result) {
                    item.put("title", text)
                    setItem(v, item)
                    save()
                }
            }
            showDialog(dialog, true, true, true, false)
            dialog.showInput(context.getString(R.string.edit_title), titleText.text.toString())
        }

        titleText.setOnLongClickListener {
            showDialog(CustomDialog(context as Activity, context.getString(R.string.delete_item),
                    titleText.text.toString(), null) { result, _, text ->
                if(result) {
                    jsonArray?.remove(items.indexOf(item))
                    items.remove(item)
                    removeView(v)
                    save()
                }
            }, true, true, true, false)
            return@setOnLongClickListener true
        }

        setItem(v, item)
        items.add(item)
        jsonArray?.put(item)
        addView(v, childCount - 1)
    }

    private fun setItem(v: View, item: JSONObject) {
        val iconImg = v.findViewById<ImageView>(R.id.iconImg)
        val titleText = v.findViewById<TextView>(R.id.titleText)

        if(item.getLong("dtDone") != Long.MIN_VALUE) {
            iconImg.setImageResource(R.drawable.check_circle)
            titleText.paintFlags = titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else {
            iconImg.setImageResource(R.drawable.uncheck_circle)
            titleText.paintFlags = titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }
        titleText.text = item.getString("title")
    }

    private fun makeAddLy() {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_checklist_add, null, false)
        setGlobalTheme(v)
        val titleInput = v.findViewById<EditText>(R.id.titleInput)
        titleInput.hint = context.getString(R.string.add_checklist)
        titleInput.setText("")
        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                if(titleInput.text.isNotBlank()) {
                    val newItem = JSONObject()
                    newItem.put("title", titleInput.text.toString())
                    newItem.put("dtDone", Long.MIN_VALUE)
                    makeItem(newItem)
                    titleInput.setText("")
                    save()
                }
            }
            return@setOnEditorActionListener true
        }
        addView(v)
    }

    private fun save() {
        record?.links?.first { it.type == Link.Type.CHECKLIST.ordinal }?.properties = jsonArray?.toString()
    }
}