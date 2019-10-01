package com.ayaan.twelvepages.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Record
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_symbol.view.*

class SymbolListAdapter(val context: Context, private val adapterInterface: (view: View, record: SymbolManager.Symbol, action: Int) -> Unit)
    : RecyclerView.Adapter<SymbolListAdapter.ViewHolder>() {

    override fun getItemCount(): Int = SymbolManager.Symbol.values().size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_symbol, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbol = SymbolManager.Symbol.values()[position]
        val v = holder.itemView

        Glide.with(context).load(symbol.resId).into(v.iconImg)

        v.setOnClickListener { adapterInterface.invoke(v, symbol, 0) }
    }
}