package com.hellowo.journey.ui.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.*
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.MapActivity
import com.hellowo.journey.ui.dialog.*
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private var originalData: TimeObject? = null
    private val timeObject: TimeObject = TimeObject()
    private var googleMap: GoogleMap? = null
    private var targetInput : EditText? = null
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        contentPanel.visibility = View.INVISIBLE
        contentPanel.setOnClickListener {}
        initControllBtn()
        initTitle()
        initDateTime()
        initMemo()
        initStyle()
    }

    private fun initControllBtn() {
        deleteBtn.setOnClickListener { delete() }

        addOptionBtn.setOnClickListener {
            showDialog(MoreOptionDialog(MainActivity.instance!!,this@TimeObjectDetailView),
                    true, true, true, false)
        }

        colorBtn.setOnClickListener {
            showDialog(ColorPickerDialog(MainActivity.instance!!, timeObject.getColor()) { colorKey, fontColor ->
                timeObject.colorKey = colorKey
                timeObject.fontColor = fontColor
                updateUI()
            }, true, true, true, false)
        }

        previewContainer.setOnClickListener {
            showDialog(StylePickerDialog(MainActivity.instance!!, timeObject.colorKey,
                    timeObject.type, timeObject.title ?: "") { style ->
                timeObject.style = style
                updateStyleUI()
            }, true, true, true, false)
        }

        pinBtn.setOnClickListener {
            timeObject.inCalendar = !timeObject.inCalendar
            updateHeaderUI()
            updateStyleUI()
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
                if(isOpened()) {
                    if(titleInput.text.isNotEmpty()) {
                        timeObject.title = titleInput.text.toString()
                    }else {
                        timeObject.title = null
                    }
                    if(timeObject.inCalendar) updateStyleUI()
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        titleInput.isFocusableInTouchMode = false
        titleInput.clearFocus()
        titleInput.setOnClickListener { showKeyPad(titleInput) }
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
                if(isOpened()) {
                    if(memoInput.text.isNotEmpty()) {
                        timeObject.description = memoInput.text.toString()
                    }else {
                        timeObject.description = null
                    }
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        memoInput.isFocusableInTouchMode = false
        memoInput.clearFocus()
        memoInput.setOnClickListener { showKeyPad(memoInput) }
    }

    private fun initStyle() {
        val timeObjectView = TimeObjectView(context, TimeObject(), 0, 0)
        timeObjectView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        previewContainer.addView(timeObjectView, 0)
    }

    private fun updateUI() {
        colorBtn.setCardBackgroundColor(timeObject.getColor())
        fontColorText.setColorFilter(timeObject.fontColor)

        updateHeaderUI()
        updateTagUI()
        updateTitleUI()
        updateDateUI()
        updateRepeatUI()
        updateAlarmUI()
        updateLocationUI()
        updateMemoUI()
        updateStyleUI()
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
                titleInput.setSingleLine(false)
                titleInput.hint = context.getString(R.string.what_do_you_think)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            }
            else -> {
                titleInput.setSingleLine(true)
                titleInput.hint = context.getString(R.string.enter_title)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28f)
            }
        }
        titleInput.setText(timeObject.title)
        titleInput.setSelection(timeObject.title?.length ?: 0)
    }

    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    private fun updateDateUI() {
        startCal.timeInMillis = timeObject.dtStart
        endCal.timeInMillis = timeObject.dtEnd

        if(timeObject.allday) {
            if(isSameDay(startCal, endCal)) {
                durationLy.visibility = View.GONE
                timeDivider.visibility = View.GONE
                timeEndLy.visibility = View.GONE
                startSmallTimeText.visibility = View.GONE

                startBigTimeText.text = "${AppDateFormat.ymdDate.format(startCal.time)} ${AppDateFormat.dow.format(startCal.time)}"
            }else {
                durationLy.visibility = View.VISIBLE
                startSmallTimeText.visibility = View.VISIBLE
                durationText.text = (getDiffDate(startCal, endCal) + 1).toString()

                if(startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
                        && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
                    timeDivider.visibility = View.GONE
                    timeEndLy.visibility = View.GONE

                    startSmallTimeText.text = AppDateFormat.ymDate.format(startCal.time)
                    startBigTimeText.text = "${AppDateFormat.date.format(startCal.time)} - ${AppDateFormat.date.format(endCal.time)}"
                }else {
                    timeDivider.visibility = View.VISIBLE
                    timeEndLy.visibility = View.VISIBLE

                    startSmallTimeText.text = AppDateFormat.ymDate.format(startCal.time)
                    startBigTimeText.text = AppDateFormat.date.format(startCal.time)
                    endSmallTimeText.text = AppDateFormat.ymDate.format(endCal.time)
                    endBigTimeText.text = AppDateFormat.date.format(endCal.time)
                }
            }
        }else {
            if(isSameDay(startCal, endCal)) {
                durationLy.visibility = View.GONE
                timeDivider.visibility = View.GONE
                timeEndLy.visibility = View.GONE
                startSmallTimeText.visibility = View.VISIBLE

                startSmallTimeText.text = AppDateFormat.ymdeDate.format(startCal.time)
                startBigTimeText.text = "${AppDateFormat.time.format(startCal.time)} - ${AppDateFormat.time.format(endCal.time)}"
            }else {
                durationLy.visibility = View.VISIBLE
                startSmallTimeText.visibility = View.VISIBLE
                timeDivider.visibility = View.VISIBLE
                timeEndLy.visibility = View.VISIBLE
                durationText.text = (getDiffDate(startCal, endCal) + 1).toString()

                startSmallTimeText.text = AppDateFormat.ymdDate.format(startCal.time)
                startBigTimeText.text = AppDateFormat.time.format(startCal.time)
                endSmallTimeText.text = AppDateFormat.ymdDate.format(endCal.time)
                endBigTimeText.text = AppDateFormat.time.format(endCal.time)
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

    private fun updateMemoUI() {
        if(timeObject.description.isNullOrEmpty()) {
            memoInput.setText("")
            memoLy.visibility = View.GONE
        }else {
            memoInput.setText(timeObject.description)
            memoLy.visibility = View.VISIBLE
        }
    }

    private fun updateStyleUI() {
        if(timeObject.inCalendar) {
            previewContainer.visibility = View.VISIBLE
            (previewContainer.getChildAt(0) as TimeObjectView).let {
                it.timeObject.title = timeObject.title
                it.timeObject.type = timeObject.type
                it.timeObject.style = timeObject.style
                it.timeObject.colorKey = timeObject.colorKey
                it.setLookByType()

                when(it.timeObject.type) {
                    2 -> {
                        it.layoutParams.height = WRAP_CONTENT
                    }
                    else -> {
                        it.layoutParams.height = TimeObjectView.blockTypeSize
                    }
                }

                it.textSpaceWidth = it.paint.measureText(it.text.toString())
                it.requestLayout()
                it.invalidate()
            }
        }else {
            previewContainer.visibility = View.GONE
        }
    }

    fun showMemoUI() {
        memoLy.visibility = View.VISIBLE
    }

    fun confirm() {
        MainActivity.instance?.let {
            if(originalData != timeObject) {
                if(originalData?.repeat.isNullOrEmpty()) {
                    TimeObjectManager.save(timeObject)
                    it.viewModel.clearTargetTimeObject()
                }else {
                    RepeatManager.save(it, timeObject, Runnable { it.viewModel.clearTargetTimeObject() })
                }
            }else {
                it.viewModel.clearTargetTimeObject()
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
        viewMode = ViewMode.ANIMATING
        setData(timeObject)
        updateUI()
        val transitionSet = TransitionSet()
        val t1 = makeFromBottomSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(contentPanel)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.addListener(object : TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)
                viewMode = ViewMode.OPENED
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.VISIBLE
        backgroundLy.setBackgroundColor(AppTheme.primaryText)
        backgroundLy.setOnClickListener { confirm() }
        backgroundLy.isClickable = true
        contentPanel.visibility = View.VISIBLE
        if(timeObject.id.isNullOrEmpty()) {
            showKeyPad(titleInput)
        }
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        val transitionSet = TransitionSet()
        val t1 = makeFromBottomSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(contentPanel)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.INVISIBLE
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        contentPanel.visibility = View.INVISIBLE
        hideKeyPad(windowToken, titleInput)
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED

    private fun showKeyPad(v : EditText) {
        targetInput = v
        v.isFocusableInTouchMode = true
        if (titleInput.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(v, 0)
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
                    alarm.dtAlarm = timeObject.dtStart
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
        }else if (requestCode == RC_IMAGE_ATTACHMENT && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                val uri = data.data
                try{
                    MainActivity.instance?.showProgressDialog(null)
                    Glide.with(this).asBitmap().load(uri)
                            .into(object : SimpleTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap,
                                                             transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                    l("사진 크기 : ${resource.rowBytes} 바이트")
                                    val imageId = UUID.randomUUID().toString()
                                    val ref = FirebaseStorage.getInstance().reference
                                            .child("${FirebaseAuth.getInstance().uid}/$imageId.jpg")
                                    val baos = ByteArrayOutputStream()
                                    resource.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                                    val uploadTask = ref.putBytes(baos.toByteArray())
                                    uploadTask.addOnFailureListener {
                                        MainActivity.instance?.hideProgressDialog()
                                    }.addOnSuccessListener { _ ->
                                        ref.downloadUrl.addOnCompleteListener {
                                            l("다운로드 url : ${it.result.toString()}")
                                            timeObject.links.add(Link(imageId, Link.Type.IMAGE.ordinal,
                                                    null, it.result.toString(), null))
                                            MainActivity.instance?.hideProgressDialog()
                                        }
                                    }
                                }
                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    MainActivity.instance?.hideProgressDialog()
                                }
                            })
                }catch (e: Exception){}
            }
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
        val items = ArrayList<Tag>().apply { addAll(timeObject.tags) }
        showDialog(TagDialog(MainActivity.instance!!, items) {
            timeObject.tags.clear()
            timeObject.tags.addAll(it)
            updateTagUI()
        }, true, true, true, false)
    }

    fun openImagePicker() {
        MainActivity.instance?.checkExternalStoragePermission(RC_IMAGE_ATTACHMENT)
    }
}