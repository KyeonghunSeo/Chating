package com.hellowo.journey.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.R
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.list_item_time_object.view.*
import org.json.JSONObject
import java.util.*


class TimeObjectListAdapter(val context: Context, val items: List<TimeObject>,
                            val adapterInterface: (view: View, timeObject: TimeObject, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            /*
            container.checkBox.imageAssetsFolder = "assets/"
            container.checkBox.setAnimation("checked_done.json")
            container.checkBox.visibility = View.GONE
            */
        }

        fun onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            //itemView.setBackgroundColor(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_time_object, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        if(timeObject.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
        }else {
            v.tagText.visibility = View.GONE
        }

        v.titleText.text = if(timeObject.title.isNullOrBlank()) {
            context.getString(R.string.empty_note)
        }else {
            timeObject.title
        }

        val finishTexs = StringBuilder()

        if(!timeObject.location.isNullOrBlank()) {
            finishTexs.append("${timeObject.location?.substringBefore("\n")}\n")
        }
        val updatedDate = Date(timeObject.dtUpdated)
        finishTexs.append("${AppDateFormat.ymdeDate.format(updatedDate)} ${AppDateFormat.time.format(updatedDate)}")
        v.finishText.text = finishTexs.toString()

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

        v.setOnClickListener { adapterInterface.invoke(it, timeObject, 0) }
        v.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener false
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: TimeObjectListAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            reordering = reordering or mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Fade out the view as it is swiped out of the parent's bounds
                val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
                viewHolder.itemView.translationX = dX
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is TimeObjectListAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TimeObjectListAdapter.ViewHolder?
                    itemViewHolder!!.onItemSelected()
                }
            }else if(reordering && actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                reordering = false
                TimeObjectManager.reorder(items)
            }

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
            (viewHolder as? TimeObjectListAdapter.ViewHolder)?.onItemClear()
        }
    }
}