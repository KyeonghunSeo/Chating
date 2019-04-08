package com.hellowo.journey.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hellowo.journey.*
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.list_item_time_object.view.*
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class TimeObjectListAdapter(val context: Context, val items: List<TimeObject>, val currentCal: Calendar,
                            val adapterInterface: (view: View, timeObject: TimeObject, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val tempCal = Calendar.getInstance()
    var itemTouchHelper: ItemTouchHelper? = null
    var query: String? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    class TimeObjectViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = TimeObjectViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_time_object, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.setOnClickListener { adapterInterface.invoke(it, timeObject, 0) }
        v.iconArea.setOnClickListener {
            vibrate(context)
            TimeObjectManager.done(timeObject)
        }
        v.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener false
        }

        v.iconImg.setColorFilter(timeObject.getColor())

        if(timeObject.isDone()) {
            v.iconImg.setImageResource(R.drawable.sharp_check_box_black_48dp)
            v.iconImg.alpha = 0.3f
            v.contentLy.alpha = 0.3f
            v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else {
            v.iconImg.setImageResource(R.drawable.sharp_check_box_outline_blank_black_48dp)
            v.iconImg.alpha = 1f
            v.contentLy.alpha = 1f
            v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }

        if(timeObject.isScheduled()) {
            v.timeLy.visibility = View.VISIBLE
            val totalDate = getDiffDate(timeObject.dtStart, timeObject.dtEnd) + 1
            if(totalDate == 1) {
                if(timeObject.allday) {
                    v.timeLy.visibility = View.GONE
                }else {
                    if(timeObject.dtStart == timeObject.dtEnd) {
                        v.timeText.text = AppDateFormat.time.format(Date(timeObject.dtStart))
                    }else {
                        v.timeText.text = "${AppDateFormat.time.format(Date(timeObject.dtStart))} ~ " +
                                AppDateFormat.time.format(Date(timeObject.dtEnd))
                    }
                }
            }else {
                tempCal.timeInMillis = timeObject.dtStart
                val toDateNum = getDiffDate(tempCal, currentCal)
                if(timeObject.allday) {
                    v.timeText.text = "${AppDateFormat.mdDate.format(Date(timeObject.dtStart))} ~ " +
                            AppDateFormat.mdDate.format(Date(timeObject.dtEnd)) +
                            " (${String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")})"
                }else {
                    v.timeText.text = "${AppDateFormat.dateTime.format(Date(timeObject.dtStart))} ~ " +
                            AppDateFormat.dateTime.format(Date(timeObject.dtEnd)) +
                            " (${String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")})"
                }
            }
        }else {
            v.timeLy.visibility = View.GONE
        }

        if(timeObject.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
        }else {
            v.tagText.visibility = View.GONE
        }

        if(timeObject.title.isNullOrBlank()) {
            v.titleText.text = context.getString(R.string.untitle)
        }else {
            if(!query.isNullOrEmpty()){
                highlightQuery(v.titleText, timeObject.title!!)
            }else {
                v.titleText.text = timeObject.title?.trim()
            }
        }

        if(timeObject.description.isNullOrBlank()) {
            v.memoLy.visibility = View.GONE
        }else {
            v.memoLy.visibility = View.VISIBLE
            if(!query.isNullOrEmpty()){
                highlightQuery(v.memoText, timeObject.description!!)
            }else {
                v.memoText.text = timeObject.description?.trim()
            }
        }

        if(timeObject.location.isNullOrBlank()) {
            v.locationLy.visibility = View.GONE
        }else {
            v.locationLy.visibility = View.VISIBLE
            val locText = timeObject.location?.replace("\n", " - ")
            if(!query.isNullOrEmpty()){
                highlightQuery(v.locationText, locText!!)
            }else {
                v.locationText.text = locText
            }
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
            v.imageLy.setOnClickListener { _ ->
                ImageViewer.Builder(context, list.map { it.properties })
                        .hideStatusBar(false)
                        .setStartPosition(0)
                        .show()
            }
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

    private fun highlightQuery(textView: TextView, text: String) {
        val highlightMap = HashMap<Int, Int>()
        val pattern = Pattern.compile(query)
        val matcher = pattern.matcher(text)
        val startPos = 0
        while (matcher.find()) {
            var urlStr = matcher.group()
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length - 1)
            }
            val start = text.indexOf(urlStr, startPos)
            val end = start + urlStr.length
            highlightMap[start] = end
        }
        val sb = SpannableStringBuilder()
        sb.append(text)
        for (start in highlightMap.keys) {
            sb.setSpan(BackgroundColorSpan(Color.parseColor("#50f9d073")), start, highlightMap[start]!!, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = sb
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        /* 완료된거 안움직이는 로직
        val target = items[fromPosition]
        if(!target.isDone()) {
            val limitIndex = items.indexOf(items.last { !it.isDone() })
            if(toPosition <= limitIndex) {
                Collections.swap(items, fromPosition, toPosition)
                notifyItemMoved(fromPosition, toPosition)
                return true
            }
        }
        return false
        */
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
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
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
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is TimeObjectViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TimeObjectViewHolder?
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
            (viewHolder as? TimeObjectViewHolder)?.onItemClear()
        }
    }
}