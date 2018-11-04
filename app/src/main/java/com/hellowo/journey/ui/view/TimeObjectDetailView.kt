package com.hellowo.journey.ui.view

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hellowo.journey.*
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.model.Alarm
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.calendar.RepeatManager
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.MapActivity
import com.hellowo.journey.ui.dialog.*
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import java.util.*


class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private var originalData: TimeObject? = null
    private val timeObject: TimeObject = TimeObject()
    private var googleMap: GoogleMap? = null
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        contentPanel.visibility = View.INVISIBLE
        contentPanel.setOnClickListener {}
        initControllBtn()
        initTitle()
        initDateTime()
        initMemo()
    }

    private fun initControllBtn() {
        confirmBtn.setOnClickListener {
            confirm()
        }

        deleteBtn.setOnClickListener {
            delete()
        }

        addOptionBtn.setOnClickListener {
            showDialog(MoreOptionDialog(MainActivity.instance!!,this@TimeObjectDetailView),
                    true, true, true, false)
        }

        colorBtn.setOnClickListener {
            showDialog(ColorPickerDialog(MainActivity.instance!!, timeObject.color) { color, fontColor ->
                timeObject.color = color
                timeObject.fontColor = fontColor
                updateUI()
            }, true, true, true, false)
        }

        pinBtn.setOnClickListener {
            timeObject.inCalendar = !timeObject.inCalendar
            updateHeaderUI()
        }
    }

    private fun initTitle() {
        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
            }
            return@setOnEditorActionListener false
        }
        titleInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                timeObject.title = titleInput.text.toString()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        titleInput.isFocusableInTouchMode = false
        titleInput.clearFocus()
        titleInput.setOnClickListener { showTitleKeyPad() }
    }

    private fun initDateTime() {
        timeLy.setOnClickListener {
            StartEndPickerDialog(MainActivity.instance!!, timeObject) { sCal, eCal, allday ->
                timeObject.setDateTime(allday, sCal, eCal)
                updateUI()
            }.show(MainActivity.instance?.supportFragmentManager, null)
        }
    }

    fun initMap() {
        (MainActivity.instance?.supportFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { map ->
            googleMap = map
            mapTouchView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
        }
    }

    private fun initMemo() {
        memoInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                timeObject.description = memoInput.text.toString()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun updateUI() {
        colorBtn.setCardBackgroundColor(timeObject.color)
        fontColorText.setColorFilter(timeObject.fontColor)

        updateHeaderUI()
        updateTagUI()
        updateTitleUI()
        updateDateUI()
        updateRepeatUI()
        updateAlarmUI()
        updateLocationUI()
        updateMemoUI()
    }

    private fun updateHeaderUI() {
        pinBtn.pin(timeObject.inCalendar)
    }

    private fun updateTagUI() {
        if(timeObject.tags.isNotEmpty()) {
            tagText.visibility = View.VISIBLE
            tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
        }else {
            tagText.visibility = View.GONE
        }
    }

    private fun updateTitleUI() {
        when(timeObject.type) {
            TimeObject.Type.NOTE.ordinal -> {
                titleInput.hint = context.getString(R.string.what_do_you_think)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                titleInput.typeface = AppRes.textFont
            }
            else -> {
                titleInput.hint = context.getString(R.string.title)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32f)
                titleInput.typeface = AppRes.thinFont
            }
        }
        titleInput.setText(timeObject.title)
    }

    val startCal = Calendar.getInstance()
    val endCal = Calendar.getInstance()

    private fun updateDateUI() {
        startCal.timeInMillis = timeObject.dtStart
        endCal.timeInMillis = timeObject.dtEnd

        if(timeObject.allday) {
            if(isSameDay(startCal, endCal)) {
                durationLy.visibility = View.GONE
                timeDivider.visibility = View.GONE
                timeEndLy.visibility = View.GONE
                startSmallTimeText.visibility = View.GONE

                startBigTimeText.text = "${AppRes.ymdDate.format(startCal.time)} ${AppRes.dow.format(startCal.time)}"
            }else {
                durationLy.visibility = View.VISIBLE
                startSmallTimeText.visibility = View.VISIBLE
                durationText.text = (getDiffDate(startCal, endCal) + 1).toString()

                if(startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
                        && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
                    timeDivider.visibility = View.GONE
                    timeEndLy.visibility = View.GONE

                    startSmallTimeText.text = AppRes.ymDate.format(startCal.time)
                    startBigTimeText.text = "${AppRes.date.format(startCal.time)} - ${AppRes.date.format(endCal.time)}"
                }else {
                    timeDivider.visibility = View.VISIBLE
                    timeEndLy.visibility = View.VISIBLE

                    startSmallTimeText.text = AppRes.ymDate.format(startCal.time)
                    startBigTimeText.text = "${AppRes.date.format(startCal.time)}"
                    endSmallTimeText.text = AppRes.ymDate.format(endCal.time)
                    endBigTimeText.text = "${AppRes.date.format(endCal.time)}"
                }
            }
        }else {
            if(isSameDay(startCal, endCal)) {
                durationLy.visibility = View.GONE
                timeDivider.visibility = View.GONE
                timeEndLy.visibility = View.GONE
                startSmallTimeText.visibility = View.VISIBLE

                startSmallTimeText.text = AppRes.ymdeDate.format(startCal.time)
                startBigTimeText.text = "${AppRes.time.format(startCal.time)} - ${AppRes.time.format(endCal.time)}"
            }else {
                durationLy.visibility = View.VISIBLE
                startSmallTimeText.visibility = View.VISIBLE
                timeDivider.visibility = View.VISIBLE
                timeEndLy.visibility = View.VISIBLE
                durationText.text = (getDiffDate(startCal, endCal) + 1).toString()

                startSmallTimeText.text = AppRes.ymdDate.format(startCal.time)
                startBigTimeText.text = "${AppRes.time.format(startCal.time)}"
                endSmallTimeText.text = AppRes.ymdDate.format(endCal.time)
                endBigTimeText.text = "${AppRes.time.format(endCal.time)}"
            }
        }
    }

    private fun updateRepeatUI() {
        if(timeObject.repeat.isNullOrEmpty()) {
            repeatLy.visibility = View.GONE
        }else {
            repeatLy.visibility = View.VISIBLE
            repeatText.text = RepeatManager.makeRepeatText(timeObject)
            repeatLy.setOnClickListener { openRepeatDialog() }
        }
    }

    private fun updateAlarmUI() {
        if(timeObject.alarms.isNotEmpty()) {
            alarmLy.visibility = View.VISIBLE
            alarmListView.setTimeObject(timeObject)
            alarmListView.onSelected = { openAlarmPicker(it) }
        }else {
            alarmLy.visibility = View.GONE
        }
    }

    private fun updateLocationUI() {
        if(timeObject.location.isNullOrBlank()) {
            locationLy.visibility = View.GONE
        }else {
            locationLy.visibility = View.VISIBLE
            locationText.text = timeObject.location

            val latLng = LatLng(timeObject.latitude, timeObject.longitude)
            googleMap?.clear()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            googleMap?.addMarker(MarkerOptions().position(latLng))

            locationText.setOnClickListener { openPlacePicker() }
            mapTouchView.setOnClickListener{ openMapActivity() }
        }
    }

    fun updateMemoUI() {
        if(timeObject.description.isNullOrEmpty()) {
            memoInput.setText("")
            memoLy.visibility = View.GONE
        }else {
            memoInput.setText(timeObject.description)
            memoLy.visibility = View.VISIBLE
        }
    }

    fun showMemoUI() {
        memoLy.visibility = View.VISIBLE
    }

    fun confirm() {
        MainActivity.instance?.let {
            if(originalData?.repeat.isNullOrEmpty()) {
                TimeObjectManager.save(timeObject)
                it.viewModel.clearTargetTimeObject()
            }else {
                RepeatManager.save(it, timeObject, Runnable { it.viewModel.clearTargetTimeObject() })
            }
        }
    }

    fun delete() {
        MainActivity.instance?.let {
            if(originalData?.repeat.isNullOrEmpty()) {
                TimeObjectManager.delete(timeObject)
                it.viewModel.clearTargetTimeObject()
            }else {
                RepeatManager.delete(it, timeObject, Runnable { it.viewModel.clearTargetTimeObject() })
            }
        }
    }

    private fun setData(data: TimeObject) {
        originalData = data
        timeObject.copy(data)
        if(data.id.isNullOrEmpty()) {
            deleteBtn.visibility = View.GONE
        }else {
            deleteBtn.visibility = View.VISIBLE
        }
    }

    fun show(timeObject: TimeObject) {
        l("=======SHOW DetailView=======\n$timeObject")
        viewMode = ViewMode.OPENED
        MainActivity.instance?.let {
            it.onDimDark(true, true)
        }
        //layoutParams.height = insertModeHeight
        //requestLayout()
        setData(timeObject)
        updateUI()

        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                //contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        contentPanel.visibility = View.VISIBLE

        if(timeObject.id.isNullOrEmpty()) {
            //showTitleKeyPad()
        }
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        MainActivity.instance?.let {
            it.offDimDark(true, true)
        }
        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                hideKeyPad(windowToken, titleInput)
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        //contentLy.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        contentPanel.visibility = View.INVISIBLE
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED

    private fun showTitleKeyPad() {
        titleInput.isFocusableInTouchMode = true
        if (titleInput.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(titleInput, 0)
        }
    }

    fun openPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        MainActivity.instance?.startActivityForResult(builder.build(MainActivity.instance), RC_LOCATION)
    }

    private fun openMapActivity() {
        MainActivity.instance?.let {
            val intent = Intent(it, MapActivity::class.java)
            intent.putExtra("location", timeObject.location)
            intent.putExtra("lat", timeObject.latitude)
            intent.putExtra("lng", timeObject.longitude)
            it.startActivity(intent)
        }
    }

    fun addNewAlarm() {
        val alarm = Alarm(UUID.randomUUID().toString(), timeObject.dtStart, 0, 0)
        timeObject.alarms.add(alarm)
        openAlarmPicker(alarm)
    }

    fun openAlarmPicker(alarm: Alarm) {
        AlarmPickerDialog(timeObject, alarm) { result, offset ->
            if (result) {
                alarm.offset = offset
                if (offset != Long.MIN_VALUE) {
                    alarm.dtAlarm = timeObject.dtStart + offset
                } else {
                    openDateTimePicker(context.getString(R.string.set_alarm), alarm.dtAlarm) {
                        alarm.dtAlarm = it
                        updateAlarmUI()
                    }
                }
            } else {
                timeObject.alarms.remove(alarm)
            }
            updateAlarmUI()
        }.show(MainActivity.instance?.supportFragmentManager, null)
    }

    private fun openDateTimePicker(title: String, time: Long, onResult: (Long) -> (Unit)) {
        showDialog(TimePickerDialog(MainActivity.instance!!, time, onResult),
                true, true, true, false)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_LOCATION && resultCode == RESULT_OK) {
            val place = PlacePicker.getPlace(data, context)
            timeObject.location = "${place.name}\n${place.address}"
            timeObject.latitude = place.latLng.latitude
            timeObject.longitude = place.latLng.longitude
            updateLocationUI()
        }
    }

    fun addRepeat() {
        openRepeatDialog()
    }

    private fun openRepeatDialog() {
        showDialog(RepeatDialog(MainActivity.instance!!, timeObject) { repeat, dtUntil ->
            timeObject.repeat = repeat
            timeObject.dtUntil = dtUntil
            updateRepeatUI()
        }, true, true, true, false)
    }

    fun showTagDialog() {
        showDialog(TagDialog(MainActivity.instance!!, timeObject) {
            timeObject.tags.clear()
            timeObject.tags.addAll(it)
            updateTagUI()
        }, true, true, true, false)
    }
}