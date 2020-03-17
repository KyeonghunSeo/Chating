package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.dialog.InputDialog
import kotlinx.android.synthetic.main.list_item_checklist.view.*
import kotlinx.android.synthetic.main.list_item_checklist_add.view.*
import org.json.JSONArray
import org.json.JSONObject

class CheckListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    val items = ArrayList<JSONObject>()
    var link: Link? = null
    var jsonArray: JSONArray? = null
    private var onDataChanged: ((ArrayList<JSONObject>) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter = Adapter()
        isNestedScrollingEnabled = false
    }

    fun setCheckList(l: Link, callback: ((ArrayList<JSONObject>) -> Unit)) {
        onDataChanged = callback
        items.clear()
        try{
            jsonArray = JSONArray(l.properties)
            jsonArray?.let {
                if(it.length() > 0) {
                    for(i in 0 until it.length()) {
                        items.add(it.getJSONObject(i))
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            jsonArray = JSONArray()
        }
        link = l
        adapter?.notifyDataSetChanged()
        onDataChanged?.invoke(items)
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size + 1

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init { setGlobalTheme(container) }
        }

        inner class AddViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init { setGlobalTheme(container) }
        }

        override fun getItemViewType(position: Int): Int {
            return if(position < items.size) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  {
            return if(viewType == 0) ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_checklist, parent, false))
            else AddViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_checklist_add, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(position < items.size) {
                val v = (holder as ViewHolder).itemView
                val item = items[position]

                if(item.getLong("dtDone") != Long.MIN_VALUE) {
                    v.iconImg.setImageResource(R.drawable.check)
                    v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }else {
                    v.iconImg.setImageResource(R.drawable.uncheck)
                    v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
                }
                v.titleText.text = item.getString("title")

                v.iconImg.setOnClickListener {
                    vibrate(context)
                    if(item.getLong("dtDone") == Long.MIN_VALUE) {
                        item.put("dtDone", System.currentTimeMillis())
                    }else {
                        item.put("dtDone", Long.MIN_VALUE)
                    }
                    notifyItemChanged(position)
                    save()
                }

                v.titleText.setOnClickListener {
                    val dialog = InputDialog(context as Activity,
                            R.drawable.uncheck,
                            context.getString(R.string.edit_item), null, null,
                            v.titleText.text.toString(), true) { result, text ->
                        if(result) {
                            item.put("title", text)
                            notifyItemChanged(position)
                            save()
                        }
                    }
                    showDialog(dialog, true, true, true, false)
                }

                v.titleText.setOnLongClickListener {
                    showDialog(CustomDialog(context as Activity, context.getString(R.string.delete_item),
                            v.titleText.text.toString(), null) { result, _, text ->
                        if(result) {
                            jsonArray?.remove(items.indexOf(item))
                            items.remove(item)
                            notifyItemRemoved(position)
                            save()
                        }
                    }, true, true, true, false)
                    return@setOnLongClickListener true
                }
            }else {
                val v = (holder as AddViewHolder).itemView
                v.titleInput.hint = context.getString(R.string.add_checklist)
                v.titleInput.setText("")
                v.titleInput.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == IME_ACTION_DONE) {
                        if(v.titleInput.text.isNotBlank()) {
                            val newItem = JSONObject()
                            newItem.put("title", v.titleInput.text.toString())
                            newItem.put("dtDone", Long.MIN_VALUE)
                            v.titleInput.setText("")
                            items.add(newItem)
                            jsonArray?.put(newItem)
                            notifyItemInserted(items.size - 1)
                            save()
                        }
                    }
                    return@setOnEditorActionListener true
                }
            }
        }
    }

    private fun save() {
        jsonArray?.let {
            link?.properties = it.toString()
            onDataChanged?.invoke(items)
            return@let
        }
    }

    fun allCheck() {
        if(items.any { it.getLong("dtDone") == Long.MIN_VALUE }) {
            val time = System.currentTimeMillis()
            items.forEach { it.put("dtDone", time) }
        }else {
            items.forEach { it.put("dtDone", Long.MIN_VALUE) }
        }
        adapter?.notifyDataSetChanged()
        save()
    }
}