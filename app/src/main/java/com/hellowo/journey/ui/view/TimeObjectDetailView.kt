package com.hellowo.journey.ui.view

import android.animation.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationSet
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
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
        panel.visibility = View.INVISIBLE
        folderLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        btnsLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        contentLy.setOnClickListener {}
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }
        initControllBtn()
        initInput()
        setInitiateView()
    }

    private fun initControllBtn() {
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
        titleInput.onScaleChanged = { isNormalScale ->
            val scale = if(isNormalScale) 1f else 0.7f
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(checkBox, "scaleX", checkBox.scaleX, scale),
                    ObjectAnimator.ofFloat(checkBox, "scaleY", checkBox.scaleY, scale))
            animSet.duration = 200L
            animSet.start()
        }
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

    private fun updateUI() {
        updateHeaderUI()
        updateTagUI()
        updateCheckBoxUI()
        updateCheckListUI()
        updateDeadLineUI()
        updatePercentageUI()
        updateTitleUI()
        updateDateUI()
        updateDdayUI()
        updateRepeatUI()
        updateAlarmUI()
        updateLocationUI()
        updateMemoUI()
        updateStyleUI()
        updateLinkUI()
    }

    private fun updateHeaderUI() {
        addOptionBtn.setOnClickListener {
            showDialog(MoreOptionDialog(context as Activity, timeObject, this@TimeObjectDetailView),
                    true, true, true, false)
        }

        colorBtn.setOnClickListener {
            showDialog(ColorPickerDialog(context as Activity, timeObject.getColor()) { colorKey ->
                timeObject.colorKey = colorKey
                updateUI()
            }, true, true, true, false)
        }

        pinBtn.setOnClickListener {
            timeObject.inCalendar = !timeObject.inCalendar
            if(timeObject.inCalendar) toast(R.string.show_in_calendar)
            else toast(R.string.hide_in_calendar)
            updateHeaderUI()
            updateStyleUI()
        }

        styleBtn.setOnClickListener {}

        deleteBtn.setOnClickListener {
            showDialog(CustomDialog(context as Activity, context.getString(R.string.delete),
                    context.getString(R.string.delete_sub), null) { result, _, _ ->
                if(result) delete()
            }, true, true, true, false)
        }

        moreBtn.setOnClickListener {
            moreBtn.visibility = View.GONE
            updateHeaderUI()
        }

        if(moreBtn.visibility == View.GONE) {
            colorBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            if(timeObject.folder == null) {
                pinBtn.visibility = View.VISIBLE
                if(timeObject.inCalendar) {
                    pinBtn.alpha = 1f
                    styleBtn.visibility = View.VISIBLE
                }else {
                    pinBtn.alpha = 0.3f
                    styleBtn.visibility = View.GONE
                }
            }else {
                pinBtn.visibility = View.GONE
                styleBtn.visibility = View.GONE
            }
        }

        folderImg.setColorFilter(timeObject.getColor())
        folderText.setTextColor(timeObject.getColor())
        addOptionBtn.setColorFilter(timeObject.getColor())
        colorBtn.setColorFilter(timeObject.getColor())
        pinBtn.setColorFilter(timeObject.getColor())
        styleBtn.setColorFilter(timeObject.getColor())
        deleteBtn.setColorFilter(timeObject.getColor())
        moreBtn.setColorFilter(timeObject.getColor())

        updateFolderUI()
    }

    private fun updateFolderUI() {
        if(timeObject.folder != null) {
            folderImg.setImageResource(R.drawable.folder)
            folderText.text = timeObject.folder?.name
            folderText.setOnClickListener { showFolderPickerDialog() }
        }else {
            folderImg.setImageResource(R.drawable.calendar_empty)
            folderText.text = AppDateFormat.simpleYmdDate.format(Date(timeObject.dtStart))
            folderText.setOnClickListener { showDatePickerDialog() }
        }
        folderImg.setOnClickListener {
            showDialog(CustomListDialog(context as Activity,
                    context.getString(R.string.record_position),
                    context.getString(R.string.record_position_sub),
                    null,
                    false,
                    listOf(context.getString(R.string.calendar), context.getString(R.string.folder))) { index ->
                if(index == 0) showDatePickerDialog()
                else showFolderPickerDialog()
            }, true, true, true, false)
        }
    }

    private fun showDatePickerDialog() {
        val time = if(timeObject.dtStart == Long.MIN_VALUE) System.currentTimeMillis()
        else timeObject.dtStart

        showDialog(DatePickerDialog(context as Activity, time) {
            timeObject.folder = null
            timeObject.setDate(it)
            updateHeaderUI()
            updateDateUI()
        }, true, true, true, false)
    }

    private fun showFolderPickerDialog() {
        MainActivity.getViewModel()?.folderList?.value?.let { folderList ->
            showDialog(CustomListDialog(context as Activity,
                    context.getString(R.string.folder),
                    null,
                    null,
                    false,
                    folderList.map { if(it.name.isNullOrBlank()) context.getString(R.string.untitle) else it.name!! }) { index ->
                timeObject.folder = folderList[index]
                updateHeaderUI()
            }, true, true, true, false)
        }
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

    fun updateCheckBoxUI() {
        if(timeObject.isSetCheckBox()) {
            checkBox.visibility = View.VISIBLE
            if(timeObject.isDone()) {
                checkBox.setImageResource(R.drawable.check)
            }else {
                checkBox.setImageResource(R.drawable.uncheck)
            }
            checkBox.setOnClickListener {
                if(timeObject.isDone()) timeObject.undone()
                else timeObject.done()
                updateCheckBoxUI()
            }
            checkBox.setOnLongClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.checkbox),
                        context.getString(R.string.delete_checkbox_sub), null) { result, _, _ ->
                    if(result) {
                        timeObject.clearCheckBox()
                        updateCheckBoxUI()
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }
        }else {
            checkBox.visibility = View.GONE
        }
    }

    fun updateCheckListUI() {
        if(timeObject.isSetCheckList()) {
            checkListLy.visibility = View.VISIBLE
            checkListView.setCheckList(timeObject)
        }else {
            checkListLy.visibility = View.GONE
        }
    }

    fun updateDeadLineUI() {
        if(timeObject.isSetDeadLine()) {
            deadlineLy.visibility = View.VISIBLE
            deadlineText.text = timeObject.getDdayText(System.currentTimeMillis())
            deadlineLy.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.deadline),
                        context.getString(R.string.delete_deadline_sub), null) { result, _, _ ->
                    if(result) {
                        timeObject.clearDeadLine()
                        updateDeadLineUI()
                    }
                }, true, true, true, false)
            }
        }else {
            deadlineLy.visibility = View.GONE
        }
    }

    fun updatePercentageUI() {
        if(timeObject.isSetPercentage()) {
            percentageLy.visibility = View.VISIBLE
            percentageLy.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.percentage),
                        context.getString(R.string.delete_percentage_sub), null) { result, _, _ ->
                    if(result) {
                        timeObject.clearPercentage()
                        updatePercentageUI()
                    }
                }, true, true, true, false)
            }
        }else {
            percentageLy.visibility = View.GONE
        }
    }

    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    private fun updateDateUI() {
        if(timeObject.isScheduled()) {
            timeLy.visibility = View.VISIBLE
            startCal.timeInMillis = timeObject.dtStart
            endCal.timeInMillis = timeObject.dtEnd

            timeLy.setOnClickListener { showStartEndDialog() }
            timeLy.setOnLongClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.shedule),
                        context.getString(R.string.delete_shedule_sub), null) { result, _, _ ->
                    if(result) {
                        timeObject.clearSchdule()
                        updateDateUI()
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }

            if(timeObject.allday) {
                smallStartText.text = AppDateFormat.ymDate.format(startCal.time)
                bigStartText.text = "${AppDateFormat.date.format(startCal.time)} ${AppDateFormat.simpleDow.format(startCal.time)}"
                smallEndText.text = AppDateFormat.ymDate.format(endCal.time)
                bigEndText.text = "${AppDateFormat.date.format(endCal.time)} ${AppDateFormat.simpleDow.format(endCal.time)}"
            }else {
                smallStartText.text = AppDateFormat.ymdDate.format(startCal.time)
                bigStartText.text = AppDateFormat.time.format(startCal.time)
                smallEndText.text = AppDateFormat.ymdDate.format(endCal.time)
                bigEndText.text = AppDateFormat.time.format(endCal.time)
            }

            if(startCal == endCal) {
                startEndDivider.visibility = View.GONE
                endLy.visibility = View.GONE
            }else {
                startEndDivider.visibility = View.VISIBLE
                endLy.visibility = View.VISIBLE
                durationText.text = getDurationText(startCal.timeInMillis, endCal.timeInMillis, timeObject.allday)
            }
        }else {
            timeLy.visibility = View.GONE
        }
    }

    fun showStartEndDialog() {
        showDialog(StartEndPickerDialog(context as Activity, timeObject) { sCal, eCal, allday ->
            timeObject.setSchedule()
            timeObject.setDateTime(allday, sCal, eCal)
            updateUI()
        }, true, true, true, false)
    }

    fun updateDdayUI() {
        if(timeObject.isSetDday()) {
            ddayLy.visibility = View.VISIBLE
            ddayText.text = timeObject.getDdayText(System.currentTimeMillis())
            ddayLy.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.dday),
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
        if(timeObject.isRepeat()) {
            repeatLy.visibility = View.VISIBLE
            repeatText.text = RepeatManager.makeRepeatText(timeObject)
            if(timeObject.isLunarRepeat()) {
                //repeatIcon.setImageResource(R.drawable.lunar)
                repeatLy.setOnClickListener { showLunarRepeatDialog() }
            }else {
                //repeatIcon.setImageResource(R.drawable.sharp_repeat_black_48dp)
                repeatLy.setOnClickListener { showRepeatDialog() }
            }
        }else {
            repeatLy.visibility = View.GONE
        }
    }

    fun showRepeatDialog() {
        showDialog(RepeatDialog(context as Activity, timeObject) { repeat, dtUntil ->
            timeObject.repeat = repeat
            timeObject.dtUntil = dtUntil
            updateRepeatUI()
        }, true, true, true, false)
    }

    fun showLunarRepeatDialog() {
        showDialog(LunarRepeatDialog(context as Activity, timeObject) { repeat, dtStart ->
            timeObject.setDate(dtStart)
            timeObject.repeat = repeat
            timeObject.dtUntil = Long.MIN_VALUE
            updateRepeatUI()
        }, true, true, true, false)
    }

    private fun updateAlarmUI() {
        if(timeObject.alarms.isNotEmpty()) {
            alarmLy.visibility = View.VISIBLE
            timeObject.alarms[0]?.let { alarm ->
                alarmText.text = AlarmManager.getTimeObjectAlarmText(context, alarm)
                alarmLy.setOnClickListener { showAlarmDialog(alarm) }
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

            locationText.setOnClickListener {
                showDialog(CustomDialog(context as Activity, context.getString(R.string.location),
                        null, arrayOf(context.getString(R.string.delete), context.getString(R.string.edit)))
                { result, index, _ ->
                    if(result) {
                        if(index == 0) {
                            timeObject.location = null
                            updateLocationUI()
                        }else {
                            showPlacePicker()
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

    private fun updateStyleUI() {}

    fun showMemoUI() {
        memoLy.visibility = View.VISIBLE
        showKeyPad(memoInput)
    }

    fun updateLinkUI() {
        if(timeObject.isSetLink()) {
            linkLy.visibility = View.VISIBLE
            linkListView.setList(timeObject)
        }else {
            linkLy.visibility = View.GONE
        }
    }

    private fun setData(data: TimeObject) {
        originalData = data
        timeObject.copy(data)
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
        if (v.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(v, 0)
        }
    }

    fun showPlacePicker() {
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
        showAlarmDialog(alarm)
    }

    private fun showAlarmDialog(alarm: Alarm) {
        showDialog(AlarmPickerDialog(context as Activity, timeObject, alarm) { result, offset ->
            if (result) {
                alarm.offset = offset
                if (offset != Long.MIN_VALUE) {
                    alarm.dtAlarm = timeObject.dtStart + offset
                } else {
                    alarm.dtAlarm = timeObject.dtStart
                    showTimePicker(alarm.dtAlarm) {
                        alarm.dtAlarm = it
                        updateAlarmUI()
                    }
                }
            } else {
                timeObject.alarms.remove(alarm)
            }
            updateAlarmUI()
        }, true, true, true, false)
    }

    private fun showTimePicker(time: Long, onResult: (Long) -> (Unit)) {
        showDialog(TimePickerDialog(context as Activity, time, onResult),
                true, true, true, false)
    }

    fun showEditWebsiteDialog() {
        showDialog(AddWebLinkDialog(context as Activity) { link ->
            timeObject.links.add(link)

        }, true, true, true, false)
    }

    fun showImagePicker() {
        MainActivity.instance?.checkExternalStoragePermission(RC_IMAGE_ATTACHMENT)
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
                                            updateLinkUI()
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
        t1.addTarget(panel)
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
        panel.visibility = View.VISIBLE

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
        t1.addTarget(panel)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.INVISIBLE
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        panel.visibility = View.INVISIBLE
        setInitiateView()

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

    private fun setInitiateView() {
        textEditorLy.visibility = View.GONE
        moreBtn.visibility = View.VISIBLE
        colorBtn.visibility = View.GONE
        pinBtn.visibility = View.GONE
        styleBtn.visibility = View.GONE
        deleteBtn.visibility = View.GONE
        topShadow.visibility = View.GONE
        mainScrollView.scrollTo(0,0)
    }
}