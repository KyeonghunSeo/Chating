package com.ayaan.twelvepages.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Photo
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.sheet.TagRecordSheet
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.list_item_record.view.*
import kotlinx.android.synthetic.main.list_item_record_footer.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RecordListAdapter(val context: Context, val items: ArrayList<Record>, val currentCal: Calendar,
                        val showFooter: Boolean, val adapterInterface: (view: View, record: Record, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null
    var query: String? = null
    var isSearchListMode = false
    private var footerHolder: FooterViewHolder? = null
    private val photoSideMargin = dpToPx(30)
    private val photoSize: Int = AppStatus.screenWidth / 2
    private val photoPagerMargin = -(AppStatus.screenWidth - photoSize) + dpToPx(10)

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    class RecordViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    class FooterViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
            container.footerProgress.visibility = View.GONE
            container.footerContentLy.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size + if(showFooter) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) 1 else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int) : RecyclerView.ViewHolder {
        return if(itemType == 1)
            FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_record_footer, parent, false))
        else
            RecordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_record, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == items.size) {
            footerHolder = holder as FooterViewHolder
            setFooterView()
            return
        }

        val record = items[position]
        val v = holder.itemView

        v.setOnClickListener { onItemClick(record) }
        v.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener false
        }

        if(record.isOsInstance()) {
            v.moreImg.setImageResource(R.drawable.help)
            v.moreImg.setColorFilter(AppTheme.line)
            v.moreImg.setOnClickListener { toast(R.string.this_is_os_instance) }
        }else {
            v.moreImg.setImageResource(R.drawable.more)
            v.moreImg.setColorFilter(AppTheme.disableText)
            v.moreImg.setOnClickListener { adapterInterface.invoke(v.moreImg, record, 0) }
        }

        val color = record.getColor()

        if(AppStatus.isDisplayUpdateTime) {
            v.updatedText.visibility = View.VISIBLE
            v.updatedText.text = AppDateFormat.simpleYmdDateTime.format(Date(record.dtUpdated))
        }else {
            v.updatedText.visibility = View.GONE
        }

        if(record.title.isNullOrBlank()) {
            v.titleText.visibility = View.GONE
        }else {
            v.titleText.visibility = View.VISIBLE
            if(!query.isNullOrEmpty()){
                highlightQuery(v.titleText, "　   ${record.title?.trim() ?: ""}")
            }else {
                v.titleText.text = "　   ${record.title?.trim() ?: ""}"
            }
        }

        if(record.description.isNullOrBlank()) {
            v.memoText.visibility = View.GONE
        }else {
            v.memoText.visibility = View.VISIBLE
            if(!query.isNullOrEmpty()){
                highlightQuery(v.memoText, record.description!!)
            }else {
                v.memoText.text = record.description?.trim()
            }
        }

        if(record.tags.isNotEmpty()) {
            v.tagView.visibility = View.VISIBLE
            v.tagView.isSmallTag = true
            v.tagView.post { v.tagView.setItems(record.tags, null) }
            v.tagView.onSelected = { tag, _ ->
                tag?.let { TagRecordSheet(it, record).show(MainActivity.instance!!.supportFragmentManager, null) }
            }
        }else {
            v.tagView.visibility = View.GONE
        }

        if(record.isSetCheckBox) {
            v.checkBox.setColorFilter(color)
            v.checkArea.visibility = View.VISIBLE
            if(v.titleText.text.isEmpty()) {
                v.titleText.text = "　   ${str(R.string.todo)}"
            }

            if(record.isDone()) {
                v.checkBox.setImageResource(R.drawable.checked_fill)
                if(AppStatus.checkedRecordDisplay == 2 || AppStatus.checkedRecordDisplay == 3) {
                    v.titleText.paintFlags = v.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }else {
                    v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
                }
                if(AppStatus.checkedRecordDisplay == 1 || AppStatus.checkedRecordDisplay == 3) {
                    v.titleLy.alpha = 0.4f
                }else {
                    v.titleLy.alpha = 1f
                }
            }else {
                v.checkBox.setImageResource(R.drawable.uncheck)
                v.titleLy.alpha = 1f
                v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
            }
            v.checkArea.setOnClickListener {
                vibrate(context)
                RecordManager.done(record)
            }
        }else {
            v.checkBox.setImageResource(R.drawable.normal_rect_radius)
            v.checkBox.setColorFilter(color)
            v.checkArea.visibility = View.GONE
            v.titleLy.alpha = 1f
            v.titleText.paintFlags = v.titleText.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }

        if(record.isScheduled()) {
            v.timeLy.visibility = View.VISIBLE
            v.timeImg.setImageResource(if(record.isSetTime) R.drawable.time else R.drawable.calendar)
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
                    v.timeText.text = ""
                    v.timeLy.visibility = View.GONE
                }

                if(isSearchListMode) {
                    v.timeText.text = AppDateFormat.ymde.format(Date(record.dtStart)) +
                            " ${v.timeText.text} [${getDiffTodayText(record.dtStart)}]"
                }
            }else {
                if(isSearchListMode) {
                    if(record.isSetTime) {
                        v.timeText.text = "${AppDateFormat.simpleYmdDateTime.format(Date(record.dtStart))} ~ " +
                                AppDateFormat.simpleYmdDateTime.format(Date(record.dtEnd)) +
                                " [${getDiffTodayText(record.dtStart)}]"
                    }else {
                        v.timeText.text = "${AppDateFormat.ymde.format(Date(record.dtStart))} ~ " +
                                AppDateFormat.ymde.format(Date(record.dtEnd)) +
                                " [${getDiffTodayText(record.dtStart)}]"
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
            }
        }else {
            v.timeLy.visibility = View.GONE
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

        if(record.isSetCountdown()) {
            v.ddayLy.visibility = View.VISIBLE
            v.ddayText.text = record.getCountdownText(System.currentTimeMillis())
        }else {
            v.ddayLy.visibility = View.GONE
        }

        val checkList = record.getCheckList()
        if(checkList != null) {
            val items = ArrayList<JSONObject>()
            v.checkListLy.visibility = View.VISIBLE
            try{
                JSONArray(checkList.properties).let {
                    if(it.length() > 0) {
                        for(i in 0 until it.length()) {
                            items.add(it.getJSONObject(i))
                        }
                    }
                }
                if(items.size > 0) {
                    val checkItemsCount = items.count{ it.getLong("dtDone") != Long.MIN_VALUE }
                    v.checkListText.text = "$checkItemsCount / ${items.size} ${str(R.string.doned)} - ${items.joinToString(", ", transform = {it.getString("title")})}"
                }else {
                    v.checkListLy.visibility = View.GONE
                }
            }catch (e: Exception){
                v.checkListLy.visibility = View.GONE
                e.printStackTrace()
            }
        }else {
            v.checkListLy.visibility = View.GONE
        }

        if(record.links.any { it.type == Link.Type.IMAGE.ordinal }){
            v.imageLy.visibility = View.VISIBLE
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
            /*
            v.photoPager.adapter = ColorPagerAdapter(context, list, photoSize, photoSideMargin)
            v.photoPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, (photoSize * 1.2).toInt())
            v.photoPager.offscreenPageLimit = 3
            v.photoPager.pageMargin = photoPagerMargin
            v.photoPager.setPageTransformer(true) { view, position ->
                val pageWidth = view.width
                when {
                    position > -1 && position < 0 -> {
                        view.photoView.translationX = pageWidth * position * -0.2f
                    }
                    position >= 0 -> {
                        view.photoView.translationX = pageWidth * position * -0.2f
                    }
                    else -> {
                        view.photoView.translationX = 0f
                    }
                }
            }
            v.photoPager.requestLayout()
            */
        }else {
            v.imageLy.visibility = View.GONE
        }

        if(record.isSetLink()){
            v.webLinkView.visibility = View.VISIBLE
            v.webLinkView.setList(record)
            v.webLinkView.isEnabledEdit = false
        }else {
            v.webLinkView.visibility = View.GONE
        }

        if(AppStatus.displayRecordDivider == 0) {
            v.divider.visibility = View.GONE
        }else {
            v.divider.visibility = View.VISIBLE
        }
    }

    private fun onItemClick(record: Record) {
        if(record.isOsInstance()) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, record.getOsEventId())
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
        if(fromPosition < items.size && toPosition < items.size) {
            Collections.swap(items, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    fun readyFooterView() {
        footerHolder?.let { holder ->
            holder.itemView.footerProgress.visibility = View.VISIBLE
            holder.itemView.footerContentLy.visibility = View.GONE
        }
    }

    var photoList: ArrayList<Photo>? = null
    var pastRecordsList: List<Record>? = null

    fun setFooterView(photos: ArrayList<Photo>?, pastRecords: List<Record>?) {
        photoList = photos
        pastRecordsList = pastRecords
        notifyItemChanged(items.size)
    }

    private fun setFooterView() {
        footerHolder?.let { holder ->
            val v = holder.itemView
            //TransitionManager.beginDelayedTransition(holder.itemView.footerRootLy, makeFromBottomSlideTransition())
            v.footerProgress.visibility = View.GONE
            v.footerContentLy.visibility = View.VISIBLE
            v.photoLy.visibility = View.GONE
            v.beforeYearLy.visibility = View.GONE

            when(AppStatus.rememberPhoto) {
                YES -> {
                    v.photoEmptyLy.visibility = View.GONE
                    if(photoList?.isNotEmpty() == true) {
                        v.photoLy.visibility = View.VISIBLE
                        v.photoListView.visibility = View.VISIBLE
                        v.photoListView.setList(photoList!!)
                    }else {
                        v.photoLy.visibility = View.GONE
                    }
                }
                NO -> {
                    v.photoLy.visibility = View.GONE
                }
                else ->{
                    v.photoLy.visibility = View.VISIBLE
                    v.photoEmptyLy.visibility = View.VISIBLE
                    v.photoListView.visibility = View.GONE
                    v.photoEmptyLy.setOnClickListener {
                        ActivityCompat.requestPermissions(MainActivity.instance!!,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_PHOTO_ON_DAYVIEW)
                    }
                }
            }

            pastRecordsList?.let { list ->
                list.firstOrNull()?.let { record ->
                    v.beforeYearLy.visibility = View.VISIBLE
                    v.beforeYearText.text = record.getTitleInCalendar()
                    v.beforeYearLy.setOnClickListener {
                        MainActivity.instance?.selectDate(getCalendarTime0(record.dtStart))
                        toast(R.string.moved, R.drawable.schedule)
                    }
                }
            }
        }
    }

    fun clear() {
        items.clear()
        photoList = null
        pastRecordsList = null
        notifyDataSetChanged()
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: RecordListAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = false
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
                if (viewHolder is RecordViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as RecordViewHolder?
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
            (viewHolder as? RecordViewHolder)?.onItemClear()
        }
    }
}