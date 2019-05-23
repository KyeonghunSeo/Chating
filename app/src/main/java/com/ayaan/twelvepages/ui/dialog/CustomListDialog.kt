package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_normal_list.*
import kotlinx.android.synthetic.main.list_item_normal.view.*


class CustomListDialog(activity: Activity, private val title: String, private val sub: String?,
                       private val selectedItem: String?, private val isMultiChoice: Boolean,
                       private val items: List<String>, private val onItemSelect: (Int) -> Unit) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_normal_list)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()

        titleText.text = title
        if(sub.isNullOrEmpty()) {
            subText.visibility = View.GONE
        }else {
            subText.visibility = View.VISIBLE
            subText.text = sub
        }

        if(isMultiChoice) {
            bottomBtnsLy.visibility = View.VISIBLE
            confirmBtn.setOnClickListener { _ -> dismiss() }
            cancelBtn.setOnClickListener { dismiss() }
        }else {
            bottomBtnsLy.visibility = View.GONE
        }
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView

            v.iconImg.visibility = View.GONE
            v.subText.visibility = View.GONE
            v.titleText.text = item

            if(selectedItem?.equals(item) == true) {
                v.titleText.setTextColor(AppTheme.primaryText)
            }else {
                v.titleText.setTextColor(AppTheme.primaryText)
            }

            v.setOnClickListener {
                onItemSelect.invoke(position)
                if(!isMultiChoice) {
                    dismiss()
                }
            }
        }
    }

}
