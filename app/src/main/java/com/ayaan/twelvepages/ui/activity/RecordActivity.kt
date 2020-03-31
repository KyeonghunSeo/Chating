package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.LayoutTransition.CHANGING
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.*
import com.ayaan.twelvepages.ui.sheet.MoreOptionSheet
import com.ayaan.twelvepages.ui.view.RecordView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_record.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

class RecordActivity : BaseActivity() {
    private var originalData: Record? = null
    private val record: Record = Record()
    private var googleMap: GoogleMap? = null
    private var keypadListener : Unregistrar? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        MainActivity.getViewModel()?.targetRecord?.value?.let {
            l(it.toString())
            setData(it)
            initTheme(rootLy)
            initLayout()
            initInput()
            updateUI()
            setCallAfterViewDrawed()
            MainActivity.getViewModel()?.clearTargetTimeObject()
            return
        }
        finish()
    }

    private fun setCallAfterViewDrawed() {
        callAfterViewDrawed(titleInput, Runnable{
            if(record.id.isNullOrEmpty()) {
                MainActivity.getTargetTemplate()?.let { template ->
                    if(template.isSetTitle()) {
                        titleInput.visibility = View.VISIBLE
                        if(template.recordTitleSelection <= titleInput.text.length) {
                            titleInput.setSelection(template.recordTitleSelection)
                        }
                    }else {
                        titleInput.visibility = View.GONE
                    }

                    if(template.isSetMemo()) {
                        memoInput.visibility = View.VISIBLE
                        if(template.recordMemoSelection <= memoInput.text.length) {
                            memoInput.setSelection(template.recordMemoSelection)
                        }
                    }else {
                        memoInput.visibility = View.GONE
                    }

                    if(template.isSetTitle()) {
                        showKeyPad(titleInput)
                    }else if(template.isSetMemo()) {
                        showKeyPad(memoInput)
                    }
                }
            }
        })
    }

    private fun initLayout() {
        topShadow.visibility = View.GONE
        contentLy.layoutTransition.enableTransitionType(CHANGING)
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }
        backBtn.setOnClickListener { confirm() }
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
        keypadListener = KeyboardVisibilityEvent.registerEventListener(this) { isOpen ->
            if(isOpen && memoInput.isFocused) {
                textEditorLy.visibility = View.VISIBLE
            }else {
                textEditorLy.visibility = View.GONE
            }
        }
        
        titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(titleInput.text.isNotEmpty()) {
                    record.title = titleInput.text.toString()
                }else {
                    record.title = null
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        if(record.id == null) {
            titleInput.imeOptions = EditorInfo.IME_ACTION_DONE
            titleInput.setRawInputType(InputType.TYPE_CLASS_TEXT)
            titleInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == IME_ACTION_DONE) {
                    confirm()
                }
                return@setOnEditorActionListener false
            }
        }
        titleInput.setHorizontallyScrolling(false)
        titleInput.isFocusable = true
        titleInput.isFocusableInTouchMode = true

        memoInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(memoInput.text.isNotEmpty()) {
                    record.description = memoInput.text.toString()
                }else {
                    record.description = null
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        memoInput.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initMap() {
        if(!record.location.isNullOrBlank()) {
            val supportMapFragment =  SupportMapFragment.newInstance()
            supportMapFragment.getMapAsync {
                googleMap = it
                mapTouchView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
                updateLocationUI()
            }
            supportFragmentManager.beginTransaction().replace(R.id.mapLy, supportMapFragment).commit()
        }
    }

    private fun updateUI() {
        updateHeaderUI()
        updateFolderUI()
        updateTitleUI()
        updateTagUI()
        updateMemoUI()
        updateDateUI()
        updateAlarmUI()
        updateRepeatUI()
        updateDdayUI()
        updateCheckBoxUI()
        updateCheckListUI()
        updatePercentageUI()
        updateLocationUI()
        updatePhotoUI()
        updateLinkUI()
    }

    private fun updateHeaderUI() {
        addOptionBtn.setOnClickListener {
            MoreOptionSheet(record, this).show(supportFragmentManager, null)
        }

        moreBtn.setOnClickListener {
            showDialog(PopupOptionDialog(this,
                    arrayOf(PopupOptionDialog.Item(str(R.string.share), R.drawable.export, AppTheme.secondaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)),
                    moreBtn, false) { index ->
                when(index) {
                    0 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, makeTextContentsByRecord(record))
                        shareIntent.type = "text/plain"
                        val chooser = Intent.createChooser(shareIntent, str(R.string.app_name))
                        startActivityForResult(chooser, RC_SHARE)
                    }
                    1 -> {
                        showDialog(CustomDialog(this, getString(R.string.delete),
                                getString(R.string.delete_sub), null, R.drawable.delete) { result, _, _ ->
                            if(result) delete()
                        }, true, true, true, false)
                    }
                }
            }, true, false, true, false)
        }
    }

    private fun updateFolderUI() {
        val color = record.getColor()
        val fontColor = ColorManager.getFontColor(color)

        folderText.text = record.folder?.name
        if(record.folder?.isCalendar() == true) {
            recordViewStyleBtn.visibility = View.VISIBLE
            folderText.setOnClickListener { showDatePickerDialog() }
        }else {
            recordViewStyleBtn.visibility = View.GONE
            folderText.setOnClickListener { showFolderPickerDialog() }
        }
        folderText.setOnClickListener {
            showFolderPickerDialog()
        }

        if(record.symbol.isNullOrEmpty()) {
            symbolBtn.visibility = View.GONE
        }else {
            symbolBtn.visibility = View.VISIBLE
            symbolDivider.setBackgroundColor(fontColor)
            symbolImg.setColorFilter(fontColor)
            symbolImg.setImageResource(SymbolManager.getSymbolResId(record.symbol))
            symbolBtn.setOnClickListener {showSymbolDialog() }
        }

        colorBg.setCardBackgroundColor(color)
        colorImg.setColorFilter(color)
        colorImg.setOnClickListener {
            ColorPickerDialog(record.colorKey){ colorKey ->
                record.colorKey = colorKey
                updateFolderUI()
            }.show(supportFragmentManager, null)
        }

        recordViewStyleText.text = RecordView.getStyleText(record.style)
        recordViewStyleBtn.setOnClickListener {
            showDialog(RecordViewStyleDialog(this, record, null) { style, colorKey, symbol ->
                record.style = style
                record.colorKey = colorKey
                record.symbol = symbol
                updateFolderUI()
            }, true, true, true, false)
        }

        if(symbolBtn.visibility == View.GONE && recordViewStyleBtn.visibility == View.GONE) {
            colorBg.setContentPadding(0, 0, 0, 0)
        }else {
            colorBg.setContentPadding(dpToPx(5), 0, dpToPx(5), 0)
        }
    }

    fun showSymbolDialog() {
        SymbolPickerDialog(record.symbol){
            record.symbol = it.name
            updateFolderUI()
        }.show(supportFragmentManager, null)
    }

    private fun showDatePickerDialog() {
        val time = if(record.dtStart == Long.MIN_VALUE) System.currentTimeMillis()
        else record.dtStart

        showDialog(DatePickerDialog(this, time) {
            record.folder = null
            record.setDate(it)
            updateFolderUI()
            updateDateUI()
        }, true, true, true, false)
    }

    private fun showFolderPickerDialog() {
        MainActivity.getViewModel()?.folderList?.value?.let { folderList ->
            showDialog(CustomListDialog(this,
                    getString(R.string.folder),
                    null,
                    null,
                    false,
                    folderList.map { if(it.name.isNullOrBlank()) getString(R.string.empty) else it.name!! }) { index ->
                record.folder = folderList[index]
                updateFolderUI()
            }, true, true, true, false)
        }
    }

    private fun updateTagUI() {
        if(record.tags.isNotEmpty()) {
            tagView.visibility = View.VISIBLE
            tagView.setItems(record.tags, null)
            tagView.onSelected = { _, _ -> showTagDialog() }
        }else {
            tagView.visibility = View.GONE
        }
    }

    fun showTagDialog() {
        showDialog(TagDialog(this, ArrayList(record.tags)) {
            record.tags.clear()
            record.tags.addAll(it)
            updateTagUI()
        }, true, true, true, false)
    }

    fun updateTitleUI() {
        if(record.title == null) {
            titleInput.setText("")
            titleInput.visibility = View.GONE
        }else {
            titleInput.setText(record.title)
            titleInput.setSelection(record.title?.length ?: 0)
            titleInput.visibility = View.VISIBLE
        }
    }

    fun showTitleUI() {
        titleInput.visibility = View.VISIBLE
        callAfterViewDrawed(titleInput, Runnable{ showKeyPad(titleInput) })
    }

    fun updateDdayUI() {
        if(record.isSetCountdown()) {
            ddayLy.visibility = View.VISIBLE
            ddayText.text = record.getCountdownText(System.currentTimeMillis())
            ddayLy.setOnClickListener { showCountDownDialog() }
        }else {
            ddayLy.visibility = View.GONE
        }
    }

    fun updateCheckBoxUI() {
        if(record.isSetCheckBox) {
            checkBoxLy.visibility = View.VISIBLE
            dueText.text = record.getDueText(System.currentTimeMillis())
            if(record.isDone()) {
                checkBox.setImageResource(R.drawable.check)
                checkBtn.setCardBackgroundColor(AppTheme.green)
                checkBoxText.text = str(R.string.doned)
                checkBoxText.setTextColor(AppTheme.background)
                doneImg.setColorFilter(AppTheme.background)
            }else {
                checkBox.setImageResource(R.drawable.uncheck)
                checkBtn.setCardBackgroundColor(AppTheme.lightLine)
                checkBoxText.text = str(R.string.done)
                checkBoxText.setTextColor(AppTheme.secondaryText)
                doneImg.setColorFilter(AppTheme.secondaryText)
            }
            checkBox.setOnClickListener {
                if(record.isDone()) record.undone()
                else record.done()
                updateCheckBoxUI()
            }
            checkBtn.setOnClickListener {
                if(record.isDone()) record.undone()
                else record.done()
                updateCheckBoxUI()
            }
            checkBoxLy.setOnLongClickListener {
                showDialog(CustomDialog(this, getString(R.string.delete), getString(R.string.delete_checkbox_sub),
                        null, R.drawable.delete) { result, _, _ ->
                    if(result) {
                        record.isSetCheckBox = false
                        updateCheckBoxUI()
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }
        }else {
            checkBoxLy.visibility = View.GONE
        }
    }

    fun updateCheckListUI() {
        val checkList = record.getCheckList()
        if(checkList != null) {
            checkListLy.visibility = View.VISIBLE
            checkListLy.setOnLongClickListener {
                showDialog(CustomDialog(this, getString(R.string.delete),
                        getString(R.string.delete_checklist_sub), null, R.drawable.delete) { result, _, _ ->
                    if(result) {
                        record.clearCheckList()
                        updateCheckListUI()
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }
            checkListView.setCheckList(checkList) { items -> updateCheckListUI(items) }
            allCheckBtn.setOnClickListener { checkListView.allCheck() }
        }else {
            checkListLy.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCheckListUI(items: ArrayList<JSONObject>) {
        val checkItemsCount = items.count{ it.getLong("dtDone") != Long.MIN_VALUE }
        checkListText.text = "$checkItemsCount / ${items.size} ${str(R.string.doned)}"
        if(items.size > 0 && items.size == checkItemsCount) {
            allCheckBtn.setCardBackgroundColor(AppTheme.green)
            allCheckText.text = str(R.string.all_doned)
            allCheckText.setTextColor(AppTheme.background)
            allCheckImg.setColorFilter(AppTheme.background)
        }else {
            allCheckBtn.setCardBackgroundColor(AppTheme.lightLine)
            allCheckText.text = str(R.string.all_done)
            allCheckText.setTextColor(AppTheme.secondaryText)
            allCheckImg.setColorFilter(AppTheme.secondaryText)
        }
    }

    fun updatePercentageUI() {
        if(record.isSetPercentage()) {
            percentageLy.visibility = View.VISIBLE
            percentageLy.setOnClickListener {
                showDialog(CustomDialog(this, getString(R.string.percentage),
                        getString(R.string.delete_percentage_sub), null) { result, _, _ ->
                    if(result) {
                        record.clearPercentage()
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
        if(record.isScheduled()) {
            timeLy.visibility = View.VISIBLE
            startCal.timeInMillis = record.dtStart
            endCal.timeInMillis = record.dtEnd

            startDateText.setOnClickListener { showStartEndDialog(0) }
            endDateText.setOnClickListener { showStartEndDialog(1) }
            startTimeText.setOnClickListener { showTimePickerDialog(startCal) }
            endTimeText.setOnClickListener { showTimePickerDialog(endCal) }

            startDateText.text = "${AppDateFormat.ymd.format(startCal.time)} ${AppDateFormat.simpleDow.format(startCal.time)}"
            endDateText.text = "${AppDateFormat.ymd.format(endCal.time)} ${AppDateFormat.simpleDow.format(endCal.time)}"
            durationText.text = getDurationText(startCal.timeInMillis, endCal.timeInMillis, record.isSetTime)

            if(record.isSetTime) {
                startTimeText.visibility = View.VISIBLE
                endTimeText.visibility = View.VISIBLE
                startTimeText.text = AppDateFormat.time.format(startCal.time)
                endTimeText.text = AppDateFormat.time.format(endCal.time)
                timeBtn.setOnClickListener {
                    record.setDateTime(false, startCal, endCal)
                    updateUI()
                }
                timeBtn.setPadding(0, 0, 0, 0)
                timeImg.setImageResource(R.drawable.close)
                timeText.visibility = View.GONE
                endLy.visibility = View.VISIBLE
                if(startCal == endCal) {
                    durationText.visibility = View.GONE
                }else {
                    durationText.visibility = View.VISIBLE
                }
            }else {
                startTimeText.visibility = View.GONE
                endTimeText.visibility = View.GONE
                timeBtn.setOnClickListener { showTimePickerDialog(startCal) }
                timeBtn.setPadding(dpToPx(5), 0, dpToPx(5), 0)
                timeImg.setImageResource(R.drawable.time)
                timeText.visibility = View.VISIBLE
                if(isSameDay(startCal, endCal)) {
                    endLy.visibility = View.GONE
                    durationText.visibility = View.GONE
                }else {
                    endLy.visibility = View.VISIBLE
                    durationText.visibility = View.VISIBLE
                }
            }
        }else {
            timeLy.visibility = View.GONE
        }
    }

    fun showStartEndDialog(pickerMode: Int) {
        val dialog = SchedulingDialog(this, record, pickerMode) { sCal, eCal ->
            record.setDateTime(sCal, eCal)
            updateUI()
        }
        showDialog(dialog, true, true, true, false)
    }

    private fun showTimePickerDialog(cal: Calendar) {
        showDialog(TimePickerDialog(this, cal.timeInMillis) { time ->
            cal.timeInMillis = time
            if(startCal > endCal) {
                if(cal == startCal) {
                    endCal.timeInMillis = cal.timeInMillis
                }else {
                    startCal.timeInMillis = cal.timeInMillis
                }
            }

            if(!record.isSetTime) {
                setTime1HourInterval(startCal, endCal)
            }
            record.setDateTime(true, startCal, endCal)
            updateUI()
        }, true, true, true, false)
    }

    private fun updateRepeatUI() {
        if(record.isRepeat()) {
            repeatLy.visibility = View.VISIBLE
            repeatText.text = RepeatManager.makeRepeatText(record)
            if(record.isLunarRepeat()) {
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
        showDialog(RepeatDialog(this, record) { repeat, dtUntil ->
            record.repeat = repeat
            record.dtUntil = dtUntil
            updateRepeatUI()
        }, true, true, true, false)
    }

    fun showLunarRepeatDialog() {
        showDialog(LunarRepeatDialog(this, record) { repeat, dtStart ->
            record.setDate(dtStart)
            record.repeat = repeat
            record.dtUntil = Long.MIN_VALUE
            updateRepeatUI()
        }, true, true, true, false)
    }

    private fun updateAlarmUI() {
        if(record.isSetAlarm()) {
            alarmLy.visibility = View.VISIBLE
            record.alarms[0]?.let { alarm ->
                alarmText.text = record.getAlarmText()
                alarmLy.setOnClickListener { showAlarmDialog() }
            }
        }else {
            alarmLy.visibility = View.GONE
        }
    }

    private fun updateLocationUI() {
        if(record.location.isNullOrBlank()) {
            locationLy.visibility = View.GONE
        }else {
            googleMap?.let {
                locationLy.visibility = View.VISIBLE
                locationText.text = record.location
                val latLng = LatLng(record.latitude, record.longitude)
                it.clear()
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                it.addMarker(MarkerOptions().position(latLng))

                locationText.setOnClickListener {
                    showDialog(CustomDialog(this, getString(R.string.location), null, arrayOf(getString(R.string.delete), getString(R.string.edit))) { result, index, _ ->
                        if(result) {
                            if(index == 0) {
                                record.location = null
                                updateLocationUI()
                            }else {
                                showPlacePicker()
                            }
                        }
                    }, true, true, true, false)
                }
                mapTouchView.setOnClickListener{ openMapActivity() }
                return
            }
            initMap()
        }
    }

    private fun updateMemoUI() {
        if(record.description.isNullOrEmpty()) {
            memoInput.setText("")
            memoInput.visibility = View.GONE
        }else {
            memoInput.setText(record.description)
            memoInput.visibility = View.VISIBLE
        }
    }

    fun showMemoUI() {
        memoInput.visibility = View.VISIBLE
        callAfterViewDrawed(memoInput, Runnable{ showKeyPad(memoInput) })
    }

    fun updatePhotoUI() {
        if(record.isSetPhoto()) {
            photoLy.visibility = View.VISIBLE
            photoListView.setList(record)
            addPhotoBtn.setOnClickListener { showImagePicker() }
        }else {
            photoLy.visibility = View.GONE
        }
    }

    fun updateLinkUI() {
        if(record.isSetLink()) {
            linkLy.visibility = View.VISIBLE
            linkListView.setList(record)
            addLinkBtn.setOnClickListener { showEditWebsiteDialog() }
        }else {
            linkLy.visibility = View.GONE
        }
    }

    private fun setData(data: Record) {
        originalData = data
        record.copy(data)
    }

    override fun onBackPressed() { confirm() }

    private fun confirm() {
        if(record.id.isNullOrEmpty() || originalData != record) {
            if(record.id.isNullOrEmpty() && record.isBlankText()) {
                toast(R.string.discard_blank_record, R.drawable.delete)
                finish()
            }else if(originalData?.isRepeat() == true) {
                RepeatManager.save(this, record, Runnable { savedFinish() })
            }else {
                RecordManager.save(record)
                savedFinish()
            }
        }else {
            finish()
        }
    }

    private fun delete() {
        originalData?.let { RecordManager.delete(this, it, Runnable { deletedFinish() }) }
    }

    private fun savedFinish() {
        toast(R.string.saved, R.drawable.done)
        finish()
    }

    private fun deletedFinish() {
        toast(R.string.deleted, R.drawable.delete)
        finish()
    }

    fun showPlacePicker() {
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyDqEQrjmuHV6uM26UDGvjIn05_sLBoZ4wk")
        }
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, RC_LOCATION)
    }

    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("location", record.location)
        intent.putExtra("lat", record.latitude)
        intent.putExtra("lng", record.longitude)
        startActivity(intent)
    }

    fun showAlarmDialog() {
        record.getAlarm().let {
            showDialog(AlarmPickerDialog(this, it.dayOffset, it.time, record.dtStart) { result, dayOffset, alarmTime ->
                if (result) {
                    record.setAlarm(dayOffset, alarmTime)
                } else {
                    record.removeAlarm()
                }
                updateAlarmUI()
            }, true, true, true, false)
        }
    }

    fun showCountDownDialog() {
        showDialog(CountdownDialog(this, record.getCountdown()) { result, countdown ->
            if (result) {
                record.setCountdown(countdown)
            } else {
                record.clearCountdown()
            }
            updateDdayUI()
        }, true, true, true, false)
    }

    fun showEditWebsiteDialog() {
        showDialog(AddWebLinkDialog(this) { link ->
            record.links.add(link)
            updateLinkUI()
        }, true, true, true, false)
    }

    fun showImagePicker() {
        checkExternalStoragePermission(RC_IMAGE_ATTACHMENT)
    }

    fun checkExternalStoragePermission(requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        } else {
            showPhotoPicker(requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_IMAGE_ATTACHMENT -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showPhotoPicker(requestCode) }
                return
            }
        }
    }

    private fun showPhotoPicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), requestCode)
        } catch (ex: android.content.ActivityNotFoundException) { ex.printStackTrace() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOCATION && resultCode == Activity.RESULT_OK) {
            data?.let {
                val place = Autocomplete.getPlaceFromIntent(data)
                record.location = "${place.name}\n${place.address}"
                place.latLng?.let {
                    record.latitude = it.latitude
                    record.longitude = it.longitude
                }
                updateLocationUI()
            }
        }else if (requestCode == RC_IMAGE_ATTACHMENT && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                try{
                    showProgressDialog(null)
                    Glide.with(this).asBitmap().load(uri)
                            .into(object : SimpleTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap,
                                                             transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                    l("사진 크기 : ${resource.rowBytes} 바이트")
                                    val imageId = UUID.randomUUID().toString()
                                    val ref = FirebaseStorage.getInstance().reference
                                            .child("${FirebaseAuth.getInstance().uid}/$imageId.jpg")
                                    val baos = ByteArrayOutputStream()
                                    resource.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                                    val uploadTask = ref.putBytes(baos.toByteArray())
                                    uploadTask.addOnFailureListener {
                                        hideProgressDialog()
                                    }.addOnSuccessListener {
                                        ref.downloadUrl.addOnCompleteListener {
                                            l("다운로드 url : ${it.result.toString()}")
                                            record.links.add(Link(imageId, Link.Type.IMAGE.ordinal, strParam0 = it.result.toString()))
                                            hideProgressDialog()
                                            updatePhotoUI()
                                        }
                                    }
                                }
                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    hideProgressDialog()
                                }
                            })
                }catch (e: Exception){}
            }
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

    override fun onDestroy() {
        super.onDestroy()
        keypadListener?.unregister()
    }
}