package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import kotlinx.android.synthetic.main.container_custom_list_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.list_item_small.view.*


class CustomListDialog(activity: Activity, private val title: String, private val sub: String?,
                       private val selectedItem: String?, private val isMultiChoice: Boolean,
                       private val items: List<String>, private val onItemSelect: (Int) -> Unit) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_custom_list_dlg, getScreenSize(context)[0] - dpToPx(80))
        setLayout()
    }

    private fun setLayout() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()

        titleText.text = title
        titleIcon.visibility = View.GONE
        if(sub.isNullOrEmpty()) {
            subText.visibility = View.GONE
        }else {
            subText.visibility = View.VISIBLE
            subText.text = sub
        }

        if(isMultiChoice) {
            confirmBtn.setOnClickListener {
                dismiss()
            }
            cancelBtn.setOnClickListener { dismiss() }
        }else {
            hideBottomBtnsLy()
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
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_small, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView

            v.iconImg.visibility = View.GONE
            v.subText.visibility = View.GONE
            v.checkImg.visibility = View.GONE
            v.titleText.text = item

            if(selectedItem?.equals(item) == true) {
                v.titleText.setTextColor(AppTheme.secondaryText)
            }else {
                v.titleText.setTextColor(AppTheme.secondaryText)
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
