package com.ayaan.twelvepages.adapter

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME
import android.provider.CalendarContract.EXTRA_EVENT_END_TIME
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
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.list_item_record.view.*
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class RecordListAdapter(val context: Context, val items: List<Record>, val currentCal: Calendar,
                        val adapterInterface: (view: View, record: Record, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val circlePadding = dpToPx(15)
    val checkBoxPadding = dpToPx(15)
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
            = TimeObjectViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_record, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = items[position]
        val v = holder.itemView

        v.setOnClickListener { onItemClick(record) }
        v.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener false
        }
        v.moreImg.setOnClickListener { adapterInterface.invoke(v.moreImg, record, 0) }

        v.iconImg.setColorFilter(record.getColor())
        if(record.isSetCheckBox) {
            v.iconImg.setPadding(checkBoxPadding, checkBoxPadding, checkBoxPadding, checkBoxPadding)
            if(record.isDone()) {
                v.iconImg.setImageResource(R.drawable.check)
                v.iconImg.alpha = 0.4f
                v.contentLy.alpha = 0.4f
                v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }else {
                v.iconImg.setImageResource(R.drawable.uncheck)
                v.iconImg.alpha = 1f
                v.contentLy.alpha = 1f
                v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
            }
            v.iconImg.setOnClickListener {
                vibrate(context)
                RecordManager.done(record)
            }
        }else {
            v.iconImg.setPadding(circlePadding, circlePadding, circlePadding, circlePadding)
            v.iconImg.setImageResource(R.drawable.circle_fill)
            v.iconImg.alpha = 1f
            v.contentLy.alpha = 1f
            v.iconImg.setOnClickListener(null)
            v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }

        if(record.isScheduled()) {
            v.timeLy.visibility = View.VISIBLE
            val totalDate = getDiffDate(record.dtStart, record.dtEnd) + 1
            if(totalDate == 1) {
                if(record.isSetTime) {
                    if(record.dtStart == record.dtEnd) {
                        v.timeText.text = AppDateFormat.time.format(Date(record.dtStart))
                    }else {
                        v.timeText.text = "${AppDateFormat.time.format(Date(record.dtStart))} ~ " +
                                AppDateFormat.time.format(Date(record.dtEnd))
                    }
                }else {
                    v.timeLy.visibility = View.GONE
                }
            }else {
                tempCal.timeInMillis = record.dtStart
                val toDateNum = getDiffDate(tempCal, currentCal)
                if(record.isSetTime) {
                    v.timeText.text = "${AppDateFormat.dateTime.format(Date(record.dtStart))} ~ " +
                            AppDateFormat.dateTime.format(Date(record.dtEnd)) +
                            " (${String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")})"
                }else {
                    v.timeText.text = "${AppDateFormat.md.format(Date(record.dtStart))} ~ " +
                            AppDateFormat.md.format(Date(record.dtEnd)) +
                            " (${String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")})"
                }
            }
        }else {
            v.timeLy.visibility = View.GONE
        }

        if(record.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = record.tags.joinToString("") { "#${it.title}" }
        }else {
            v.tagText.visibility = View.GONE
        }

        if(record.title.isNullOrBlank()) {
            v.titleText.text = context.getString(R.string.empty)
        }else {
            if(!query.isNullOrEmpty()){
                highlightQuery(v.titleText, record.title!!)
            }else {
                v.titleText.text = record.title?.trim()
            }
        }

        if(record.description.isNullOrBlank()) {
            v.memoLy.visibility = View.GONE
        }else {
            v.memoLy.visibility = View.VISIBLE
            if(!query.isNullOrEmpty()){
                highlightQuery(v.memoText, record.description!!)
            }else {
                v.memoText.text = record.description?.trim()
            }
        }

        if(record.location.isNullOrBlank()) {
            v.locationLy.visibility = View.GONE
        }else {
            v.locationLy.visibility = View.VISIBLE
            val locText = record.location?.replace("\n", " - ")
            if(!query.isNullOrEmpty()){
                highlightQuery(v.locationText, locText!!)
            }else {
                v.locationText.text = locText
            }
        }

        if(record.alarms.isNotEmpty()) {
            v.alarmText.text = record.getAlarmText()
            v.alarmLy.visibility = View.VISIBLE
        }else {
            v.alarmLy.visibility = View.GONE
        }

        if(!record.repeat.isNullOrBlank()) {
            v.repeatLy.visibility = View.VISIBLE
            v.repeatText.text = RepeatManager.makeRepeatText(record)
        }else {
            v.repeatLy.visibility = View.GONE
        }

        if(record.links.any { it.type == Link.Type.IMAGE.ordinal }){
            val list = record.links.filter{ it.type == Link.Type.IMAGE.ordinal }

            Glide.with(context).load(list[0].strParam0).into(v.mainImgView)

            if(list.size > 1) {
                v.subImageLy.visibility = View.VISIBLE
                Glide.with(context).load(list[1].strParam0).into(v.subImageView)
                if(list.size > 2) {
                    v.subImageText.text = "+${list.size - 2}"
                    v.subImageText.visibility = View.VISIBLE
                }else {
                    v.subImageText.visibility = View.GONE
                }
            }
            else v.subImageLy.visibility = View.GONE

            v.imageLy.visibility = View.VISIBLE
            v.imageLy.setOnClickListener {
                ImageViewer.Builder(context, list.map { it.strParam0 })
                        .hideStatusBar(false)
                        .setStartPosition(0)
                        .show()
            }
        }else {
            v.imageLy.visibility = View.GONE
        }

        if(record.links.any { it.type == Link.Type.WEB.ordinal }){
            val link = record.links.first{ it.type == Link.Type.WEB.ordinal }
            val url = link.strParam0
            val imageurl = link.strParam1
            val favicon = link.strParam2

            v.linkText.text = link.title
            if(!imageurl.isNullOrBlank())
                Glide.with(context).load(imageurl).into(v.linkImg)
            else if(!favicon.isNullOrBlank())
                Glide.with(context).load(favicon).into(v.linkImg)
            else {
                Glide.with(context).load(R.drawable.website).into(v.linkImg)
            }

            v.linkLy.visibility = View.VISIBLE
            v.linkLy.setOnClickListener {
                try{
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }catch (e: Exception) {
                    toast(R.string.invalid_info)
                }
            }
        }else {
            v.linkLy.visibility = View.GONE
        }
    }

    private fun onItemClick(record: Record) {
        if(record.id?.startsWith("osInstance::") == true) {
            val eventId = record.id?.substring("osInstance::".length, record.id!!.length)?.toLong() ?: -1
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val intent = Intent(Intent.ACTION_VIEW).setData(uri)
            if(record.isSetTime) {
                intent.putExtra(EXTRA_EVENT_BEGIN_TIME, record.dtStart)
                intent.putExtra(EXTRA_EVENT_END_TIME, record.dtEnd)
            }else {
                intent.putExtra(EXTRA_EVENT_BEGIN_TIME, record.dtUpdated)
                intent.putExtra(EXTRA_EVENT_END_TIME, record.dtCreated)
            }
            MainActivity.instance?.startActivityForResult(intent, RC_OS_CALENDAR)
        }else {
            MainActivity.getViewModel()?.targetRecord?.value = record
        }
    }

    @SuppressLint("UseSparseArrays")
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

    inner class SimpleItemTouchHelperCallback(private val mAdapter: RecordListAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true
        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            reordering = reordering or mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

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
                RecordManager.reorder(items)
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