package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
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
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Alarm
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.MapActivity
import com.hellowo.journey.ui.dialog.*
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
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
        initInput()
    }

    private fun updateUI() {
        updateHeaderUI()
        updateTagUI()
        updateTitleUI()
        updateDateUI()
        updateDdayUI()
        updateRepeatUI()
        updateAlarmUI()
        updateLocationUI()
        updateMemoUI()
        updateStyleUI()
    }

    private fun initControllBtn() {
        val timeObjectView = TimeObjectView(context, TimeObject(), 0, 0)
        timeObjectView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        previewContainer.addView(timeObjectView, 0)

        deleteBtn.setOnClickListener { delete() }

        addOptionBtn.setOnClickListener {
            showDialog(MoreOptionDialog(context as Activity,timeObject, this@TimeObjectDetailView),
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

        editorTimeBtn.setOnClickListener { editorAction("time") }
        editorQuoteBtn.setOnClickListener { editorAction("quote") }
        editorQuotesBtn.setOnClickListener { editorAction("quotes") }
        editorDotBtn.setOnClickListener { editorAction("dot") }
        editorUpBtn.setOnClickListener { editorAction("up") }
        editorDownBtn.setOnClickListener { editorAction("down") }
        editorLeftBtn.setOnClickListener { editorAction("left") }
        editorRightBtn.setOnClickListener { editorAction("right") }
    }

    private fun initInput() {
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

    fun initMap() {
        (MainActivity.instance?.supportFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { map ->
            googleMap = map
            mapTouchView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
        }
    }

    private fun updateHeaderUI() {
        colorBtn.setColorFilter(timeObject.getColor())
        pinBtn.pin(timeObject.inCalendar)
    }

    private fun updateTagUI() {
        if(timeObject.tags.isNotEmpty()) {
            tagText.visibility = View.VISIBLE
            tagText.text = timeObject.tags.joinToString("") { "#${it.id}" }
            tagText.setOnClickListener { showTagDialog() }
        }else {
            tagText.visibility = View.GONE
            tagText.setOnClickListener(null)
        }
    }

    fun showTagDialog() {
        val items = ArrayList<Tag>().apply { addAll(timeObject.tags) }
        showDialog(TagDialog(MainActivity.instance!!, items) {
            timeObject.tags.clear()
            timeObject.tags.addAll(it)
            updateTagUI()
        }, true, true, true, false)
    }

    private fun updateTitleUI() {
        titleInput.setText(timeObject.title)
        titleInput.setSelection(timeObject.title?.length ?: 0)
    }

    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    private fun updateDateUI() {
        startCal.timeInMillis = timeObject.dtStart
        endCal.timeInMillis = timeObject.dtEnd

        timeLy.setOnClickListener {
            showDialog(StartEndPickerDialog(MainActivity.instance!!, timeObject) { sCal, eCal, allday ->
                timeObject.setDateTime(allday, sCal, eCal)
                updateUI()
            }, true, true, true, false)
        }

        val timeString = StringBuilder()
        val durationString = StringBuilder()
        val unitString = StringBuilder()

        if(isSameDay(startCal, endCal)) {
            timeString.append(AppDateFormat.ymdeDate.format(startCal.time))
            if(!timeObject.allday) {
                if(startCal.timeInMillis == endCal.timeInMillis) {
                    timeString.append(AppDateFormat.time.format(startCal.time))
                }else {
                    durationString.append((endCal.timeInMillis - startCal.timeInMillis) / MIN_MILL)
                    unitString.append(context.getString(R.string.min))
                    timeString.append("\n${AppDateFormat.time.format(startCal.time)} ~ ${AppDateFormat.time.format(endCal.time)}")
                }
            }
        }else {
            durationString.append((getDiffDate(startCal, endCal) + 1))
            unitString.append(context.getString(R.string.date_duration))

            if(timeObject.allday) {
                timeString.append(String.format(context.getString(R.string.from_to),
                        AppDateFormat.ymdeDate.format(startCal.time), AppDateFormat.ymdeDate.format(endCal.time)))
            }else {
                timeString.append(String.format(context.getString(R.string.from_to),
                        "${AppDateFormat.ymdeDate.format(startCal.time)} ${AppDateFormat.time.format(startCal.time)}",
                        "${AppDateFormat.ymdeDate.format(endCal.time)} ${AppDateFormat.time.format(endCal.time)}"))
            }
        }

        if(durationString.isNotEmpty()) {
            durationLy.visibility = View.GONE
            durationText.text = durationString.toString()
            unitText.text = unitString.toString()
        }else {
            durationLy.visibility = View.GONE
        }

        timeText.text = timeString.toString()
    }

    fun updateDdayUI() {
        if(timeObject.isSetDday()) {
            ddayLy.visibility = View.VISIBLE
            ddayText.text = timeObject.getDdayText(System.currentTimeMillis())
            ddayLy.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.delete_dday),
                        context.getString(R.string.delete_dday_sub), null) { result, _, _ ->
                    if(result) {
                        timeObject.clearDday()
                        updateDdayUI()
                    }
                }, true, true, true, false)
            }
        }else {
            ddayLy.visibility = View.GONE
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

    fun openRepeatDialog() {
        showDialog(RepeatDialog(MainActivity.instance!!, timeObject) { repeat, dtUntil ->
            timeObject.repeat = repeat
            timeObject.dtUntil = dtUntil
            updateRepeatUI()
        }, true, true, true, false)
    }

    private fun updateAlarmUI() {
        if(timeObject.alarms.isNotEmpty()) {
            alarmLy.visibility = View.VISIBLE
            timeObject.alarms[0]?.let { alarm ->
                alarmText.text = AlarmManager.getTimeObjectAlarmText(context, alarm)
                alarmLy.setOnClickListener { openAlarmPicker(alarm) }
            }
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

            locationBtn.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.location),
                        null, arrayOf(context.getString(R.string.delete), context.getString(R.string.edit)))
                { result, index, _ ->
                    if(result) {
                        if(index == 0) {
                            timeObject.location = null
                            updateLocationUI()
                        }else {
                            openPlacePicker()
                        }
                    }
                }, true, true, true, false)
            }
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

    private fun setData(data: TimeObject) {
        originalData = data
        timeObject.copy(data)
        if(data.id.isNullOrEmpty()) {
            deleteBtn.visibility = View.GONE
        }else {
            deleteBtn.visibility = View.VISIBLE
        }
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

    private fun openAlarmPicker(alarm: Alarm) {
        AlarmPickerDialog(timeObject, alarm) { result, offset ->
            if (result) {
                alarm.offset = offset
                if (offset != Long.MIN_VALUE) {
                    alarm.dtAlarm = timeObject.dtStart + offset
                } else {
                    alarm.dtAlarm = timeObject.dtStart
                    openTimePicker(context.getString(R.string.set_alarm), alarm.dtAlarm) {
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

    private fun openTimePicker(title: String, time: Long, onResult: (Long) -> (Unit)) {
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
                                                    null, it.result.toString()))
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

    fun openImagePicker() {
        MainActivity.instance?.checkExternalStoragePermission(RC_IMAGE_ATTACHMENT)
    }

    fun setKeyboardLy(isOpen: Boolean) {
        if(isOpen) {
            textEditorLy.visibility = View.GONE
        }else {
            textEditorLy.visibility = View.GONE
        }
    }

    private fun editorAction(action: String) {
        val v = if(titleInput.isFocused) titleInput else if(memoInput.isFocused) memoInput else null
        v?.let {
            when(action) {
                "time" -> {
                    val text = AppDateFormat.time.format(Date())
                    val start = Math.max(v.selectionStart, 0)
                    val end = Math.max(v.selectionEnd, 0)
                    v.text.replace(Math.min(start, end), Math.max(start, end), text, 0, text.length)
                    return@let
                }
                "quote" -> {
                    val text = "“”"
                    val start = Math.max(v.selectionStart, 0)
                    val end = Math.max(v.selectionEnd, 0)
                    v.text.replace(Math.min(start, end), Math.max(start, end), text, 0, text.length)
                    v.setSelection(v.selectionStart - 1)
                    return@let
                }
                "quotes" -> {
                    val text = "‘’"
                    val start = Math.max(v.selectionStart, 0)
                    val end = Math.max(v.selectionEnd, 0)
                    v.text.replace(Math.min(start, end), Math.max(start, end), text, 0, text.length)
                    v.setSelection(v.selectionStart - 1)
                    return@let
                }
                "dot" -> {
                    val text = "\n ㆍ "
                    val start = Math.max(v.selectionStart, 0)
                    val end = Math.max(v.selectionEnd, 0)
                    v.text.replace(Math.min(start, end), Math.max(start, end), text, 0, text.length)
                    return@let
                }
                "left" -> {
                    if(v.selectionStart > 0) v.setSelection(v.selectionStart - 1)
                }
                "up" -> {
                    val start = Math.max(v.selectionStart, 0)
                    val layout = v.layout
                    val currentLine = layout.getLineForOffset(start)
                    if(currentLine > 0) {
                        val offset = start - layout.getLineStart(currentLine)
                        val s = layout.getLineStart(currentLine - 1)
                        val e = layout.getLineEnd(currentLine - 1)
                        v.setSelection(if(s + offset <= e) s + offset else e)
                    }
                }
                "right" -> {
                    if(v.selectionStart < v.text.length) v.setSelection(v.selectionStart + 1)
                }
                "down" -> {
                    val start = Math.max(v.selectionStart, 0)
                    val layout = v.layout
                    val currentLine = layout.getLineForOffset(start)
                    if(currentLine < v.lineCount - 1) {
                        val offset = start - layout.getLineStart(currentLine)
                        val s = layout.getLineStart(currentLine + 1)
                        val e = layout.getLineEnd(currentLine + 1)
                        v.setSelection(if(s + offset <= e) s + offset else e)
                    }
                }
            }
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
        t1.duration = ANIM_DUR
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MainActivity.instance?.window?.let { window ->
                var flags = window.peekDecorView().systemUiVisibility
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                window.peekDecorView().systemUiVisibility = flags
                window.statusBarColor = context.getColor(R.color.transitionDim)
            }
        }

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
        t1.duration = ANIM_DUR
        t1.addTarget(contentPanel)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.INVISIBLE
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        contentPanel.visibility = View.INVISIBLE
        textEditorLy.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MainActivity.instance?.window?.let { window ->
                var flags = window.peekDecorView().systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.peekDecorView().systemUiVisibility = flags
                window.statusBarColor = AppTheme.backgroundColor
            }
        }

        hideKeyPad(windowToken, titleInput)
    }
}