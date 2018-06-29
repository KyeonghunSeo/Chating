package com.hellowo.chating.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.chating.R
import com.hellowo.chating.model.ChatRoom
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.list_item_normal_check.view.*

class ChatRoomAdapter(val context: Context,
                      val items: OrderedRealmCollection<ChatRoom>,
                      val adapterInterface: (chatRoom: ChatRoom) -> Unit) : RealmRecyclerViewAdapter<ChatRoom, ChatRoomAdapter.ViewHolder>(items, true) {

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_check, parent, false))

    override fun onBindViewHolder(holder: ChatRoomAdapter.ViewHolder, position: Int) {
        val chatRoom = items[position]
        val v = holder.itemView

        v.titleText.text = chatRoom.name

        v.setOnClickListener { adapterInterface.invoke(chatRoom) }
    }
}