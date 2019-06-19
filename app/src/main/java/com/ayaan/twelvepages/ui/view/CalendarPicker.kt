package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.listener.MainDragAndDropListener
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.DateInfoManager
import java.util.*

class CalendarPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val columns = 7
    }

    private val calendarLy = LinearLayout(context)
    private val weekLys = Array(6) { FrameLayout(context) }
    private val dateLys = Array(6) { LinearLayout(context) }
    private val dateCells = Array(maxCellNum) { FrameLayout(context) }
    private val dateHeaders = Array(maxCellNum) {
        DateHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_calendar_picker_date, null, false)) }
    private val dateInfos = Array(maxCellNum) { DateInfoManager.DateInfo() }

    inner class DateHeaderViewHolder(val container: View) {
        val dateText: TextView = container.findViewById(R.id.dateText)
        val selectImg: ImageView = container.findViewById(R.id.selectImg)
        val leftImg: ImageView = container.findViewById(R.id.leftImg)
        val rightImg: ImageView = container.findViewById(R.id.rightImg)
        init {
            dateText.typeface = AppTheme.regularCFont
            container.setBackgroundResource(AppTheme.selectableItemBackground)
        }
    }

    private val tempCal = Calendar.getInstance()
    private val monthCal = Calendar.getInstance()
    private val todayCal = Calendar.getInstance()
    val targetCal: Calendar = Calendar.getInstance()

    var targetCellNum = -1
    var todayCellNum = -1
    val cellTimeMills = LongArray(maxCellNum) { Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    var onSelectedDate: ((Long) -> Unit)? = null
    var onClicked: ((Int, Int, Int) -> Unit)? = null
    var startCellNum = 0
    var endCellNum = 0
    var minCalendarHeight = 0f
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0
    var todayStatus = 0
    var sundayPos = 0
    var saturdayPos = 6

    init {
        createViews()
        setLayout()
    }

    private fun createViews() {
        addView(calendarLy)
    }

    private fun setLayout() {
        setBackgroundColor(AppTheme.background)
        calendarLy.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(0, 0, 0, 0)
        calendarLy.orientation = LinearLayout.VERTICAL

        for(i in 0..5) {
            val weekLy = weekLys[i]
            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

            for (j in 0..6){
                val cellNum = i*7 + j
                val dateCell = dateCells[cellNum]
                dateCell.setOnClickListener { onDateClick(cellNum) }
                dateCell.setOnLongClickListener {
                    if(targetCellNum != cellNum) onDateClick(cellNum)
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateCell.addView(dateHeaders[cellNum].container)
                dateLy.addView(dateCell)
            }

            weekLy.addView(dateLy)
            calendarLy.addView(weekLy)
        }
    }

    fun draw(time: Long) {
        targetCal.timeInMillis = time
        if(width > 0) {
            drawCalendar(time)
        }else {
            callAfterViewDrawed(this, Runnable { drawCalendar(time) })
        }
    }

    private fun drawCalendar(time: Long) {
        val t = System.currentTimeMillis()
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        tempCal.set(Calendar.DATE, 1)
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - AppStatus.startDayOfWeek
        if (startCellNum < 0) { startCellNum += 7 }
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        todayCellNum = -1
        targetCellNum = -1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height.toFloat()
        minWidth = (width.toFloat()) / columns
        minHeight = minCalendarHeight / rows
        if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            sundayPos = 0
            saturdayPos = 6
        } else {
            sundayPos = 8 - AppStatus.startDayOfWeek
            saturdayPos = 7 - AppStatus.startDayOfWeek
        }

        tempCal.add(Calendar.DATE, -startCellNum)

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    val dateInfo = dateInfos[cellNum]
                    DateInfoManager.getHoliday(dateInfo, tempCal)
                    if(isSameDay(tempCal, targetCal)) { targetCellNum = cellNum }
                    if(isSameDay(tempCal, todayCal)) { todayCellNum = cellNum }
                    val dateText = dateHeaders[cellNum].dateText
                    val color = getDateTextColor(cellNum, dateInfo.holiday?.isHoli == true)
                    val alpha = if(cellNum in startCellNum..endCellNum) 1f else AppStatus.outsideMonthAlpha
                    dateText.alpha = alpha

                    dateText.text = String.format("%02d", tempCal.get(Calendar.DATE))
                    dateText.setTextColor(color)

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                    tempCal.add(Calendar.DATE, 1)
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }
    }

    private fun getDateTextColor(cellNum: Int, isHoli: Boolean) : Int {
        return if(isHoli || cellNum % columns == sundayPos) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
    }



    private fun onDateClick(cellNum: Int) {
        tempCal.timeInMillis = cellTimeMills[cellNum]
        selectDate(cellNum)
        onClicked?.invoke(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
    }

    fun unselectDate() {
        if(targetCellNum >= 0) unselectDate(targetCellNum)
    }

    fun unselectDate(cellNum: Int) {
        targetCellNum = -1
    }

    fun selectDate() {
        selectDate(if(todayCellNum >= startCellNum) todayCellNum else startCellNum)
    }

    fun selectTime(time: Long) {
        selectDate(((time - calendarStartTime) / DAY_MILL).toInt())
    }

    @SuppressLint("SetTextI18n")
    fun selectDate(cellNum: Int) {
        targetCal.timeInMillis = cellTimeMills[cellNum]
        if(targetCellNum >= 0) unselectDate(targetCellNum)
        targetCellNum = cellNum
        todayStatus = getDiffToday(targetCal)
        onSelectedDate?.invoke(cellTimeMills[cellNum])
    }

    fun drawRange(color: Int, startTime: Long, endTime: Long) {
        val s = ((startTime - calendarStartTime) / DAY_MILL).toInt()
        val e = ((endTime - calendarStartTime) / DAY_MILL).toInt()
        dateHeaders.forEachIndexed { index, holder ->
            when (index) {
                in s..e -> {
                    val fontColor = AppTheme.getFontColor(color)
                    holder.selectImg.setColorFilter(color)
                    holder.rightImg.setColorFilter(color)
                    holder.leftImg.setColorFilter(color)
                    when {
                        index == s && index == e -> {
                            holder.selectImg.visibility = View.VISIBLE
                            holder.leftImg.visibility = View.INVISIBLE
                            holder.rightImg.visibility = View.INVISIBLE
                            holder.selectImg.setImageResource(R.drawable.circle_fill)
                        }
                        index == s -> {
                            holder.selectImg.visibility = View.VISIBLE
                            holder.leftImg.visibility = View.INVISIBLE
                            holder.rightImg.visibility = View.VISIBLE
                            holder.rightImg.setImageResource(R.drawable.normal_rect_fill)
                            holder.selectImg.setImageResource(R.drawable.circle_fill)
                        }
                        index == e -> {
                            holder.selectImg.visibility = View.VISIBLE
                            holder.leftImg.visibility = View.VISIBLE
                            holder.rightImg.visibility = View.INVISIBLE
                            holder.selectImg.setImageResource(R.drawable.circle_fill)
                            holder.leftImg.setImageResource(R.drawable.normal_rect_fill)
                        }
                        else -> {
                            holder.selectImg.visibility = View.INVISIBLE
                            holder.leftImg.visibility = View.VISIBLE
                            holder.rightImg.visibility = View.VISIBLE
                            holder.leftImg.setImageResource(R.drawable.normal_rect_fill)
                            holder.rightImg.setImageResource(R.drawable.normal_rect_fill)
                        }
                    }
                    holder.dateText.setTextColor(fontColor)
                }
                else -> {
                    val dateInfo = dateInfos[index]
                    val fontColor = getDateTextColor(index, dateInfo.holiday?.isHoli == true)
                    holder.selectImg.visibility = View.INVISIBLE
                    holder.rightImg.visibility = View.INVISIBLE
                    holder.leftImg.visibility = View.INVISIBLE
                    holder.dateText.setTextColor(fontColor)
                }
            }
        }
    }

}