package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
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
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_record.*
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
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
                    val initSelection = template.recordTitleSelection
                    if(initSelection <= titleInput.text.length) titleInput.setSelection(initSelection)
                    if(template.isSetMemo()) memoLy.visibility = View.VISIBLE
                }
                showKeyPad(titleInput)
            }
        })
    }

    private fun initLayout() {
        topShadow.visibility = View.GONE
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
        /*
        keypadListener = KeyboardVisibilityEvent.registerEventListener(this) { isOpen ->
            l("키보드 상태 $isOpen")
            if(isOpen) {

            }else {

            }
        }
        */
        
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
        titleInput.onScaleChanged = { isNormalScale ->
            if(isNormalScale) {

            }else {

            }
        }
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
        updateCheckBoxUI()
        updateCheckListUI()
        updatePercentageUI()
        updateDateUI()
        updateDdayUI()
        updateRepeatUI()
        updateAlarmUI()
        updateLocationUI()
        updateMemoUI()
        updateLinkUI()
    }

    private fun updateHeaderUI() {
        addOptionBtn.setOnClickListener {
            showDialog(MoreOptionDialog(this, record, this),
                    true, true, true, false)
        }

        moreBtn.setOnClickListener {
            showDialog(PopupOptionDialog(this,
                    arrayOf(PopupOptionDialog.Item(str(R.string.share), R.drawable.share, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)),
                    moreBtn) { index ->
                when(index) {
                    0 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, makeShareContentsByRecord(record))
                        shareIntent.type = "text/plain"
                        val chooser = Intent.createChooser(shareIntent, str(R.string.app_name))
                        startActivityForResult(chooser, RC_SHARE)
                    }
                    1 -> {
                        showDialog(CustomDialog(this, getString(R.string.delete),
                                getString(R.string.delete_sub), null) { result, _, _ ->
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
        colorBtn.setColorFilter(color)

        if(record.folder?.isCalendar() == true) {
            folderText.text = AppDateFormat.simpleYmdDate.format(Date(record.dtStart))
            folderText.setOnClickListener { showDatePickerDialog() }
        }else {
            folderText.text = record.folder?.name
            folderText.setOnClickListener { showFolderPickerDialog() }
            styleBtn.visibility = View.GONE
        }

        folderText.setOnClickListener {
            showFolderPickerDialog()
        }

        colorBtn.setOnClickListener {
            ColorPickerDialog(record.colorKey){ colorKey ->
                record.colorKey = colorKey
                updateFolderUI()
            }.show(supportFragmentManager, null)
            /*
            val location = IntArray(2)
            colorBtn.getLocationOnScreen(location)
            showDialog(SmallColorPickerDialog(this, record.colorKey, location) { colorKey ->
                record.colorKey = colorKey
                updateFolderUI()
            }, true, true, true, false)
            */
        }

        styleBtn.setOnClickListener {
            showDialog(RecordViewStyleDialog(this, record, null) { style, colorKey ->
                record.style = style
                record.colorKey = colorKey
                updateFolderUI()
            }, true, true, true, false)
        }
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
            tagText.visibility = View.VISIBLE
            tagText.text = record.tags.joinToString("") { "#${it.title}" }
            tagText.setOnClickListener { showTagDialog() }
        }else {
            tagText.visibility = View.GONE
            tagText.setOnClickListener(null)
        }
    }

    fun showTagDialog() {
        showDialog(TagDialog(this, ArrayList(record.tags)) {
            record.tags.clear()
            record.tags.addAll(it)
            updateTagUI()
        }, true, true, true, false)
    }

    private fun updateTitleUI() {
        titleInput.setText(record.title)
        titleInput.setSelection(record.title?.length ?: 0)
    }

    fun updateCheckBoxUI() {
        if(record.isSetCheckBox()) {
            checkBox.visibility = View.VISIBLE
            if(record.isDone()) {
                checkBox.setImageResource(R.drawable.check)
            }else {
                checkBox.setImageResource(R.drawable.uncheck)
            }
            checkBox.setOnClickListener {
                if(record.isDone()) record.undone()
                else record.done()
                updateCheckBoxUI()
            }
            checkBox.setOnLongClickListener {
                showDialog(CustomDialog(this, getString(R.string.checkbox),
                        getString(R.string.delete_checkbox_sub), null) { result, _, _ ->
                    if(result) {
                        record.clearCheckBox()
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
        if(record.isSetCheckList()) {
            checkListLy.visibility = View.VISIBLE
            checkListView.setCheckList(record)
        }else {
            checkListLy.visibility = View.GONE
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

            timeLy.setOnClickListener { showStartEndDialog() }
            timeLy.setOnLongClickListener {
                showDialog(CustomDialog(this, getString(R.string.shedule),
                        getString(R.string.delete_shedule_sub), null) { result, _, _ ->
                    if(result) {
                        record.clearSchdule()
                        updateDateUI()
                    }
                }, true, true, true, false)
                return@setOnLongClickListener true
            }

            if(record.isSetTime()) {
                smallStartText.text = AppDateFormat.ymd.format(startCal.time)
                bigStartText.text = AppDateFormat.time.format(startCal.time)
                smallEndText.text = AppDateFormat.ymd.format(endCal.time)
                bigEndText.text = AppDateFormat.time.format(endCal.time)
            }else {
                smallStartText.text = AppDateFormat.ym.format(startCal.time)
                bigStartText.text = "${AppDateFormat.date.format(startCal.time)} ${AppDateFormat.simpleDow.format(startCal.time)}"
                smallEndText.text = AppDateFormat.ym.format(endCal.time)
                bigEndText.text = "${AppDateFormat.date.format(endCal.time)} ${AppDateFormat.simpleDow.format(endCal.time)}"
            }

            if(startCal == endCal) {
                startEndDivider.visibility = View.GONE
                endLy.visibility = View.GONE
            }else {
                startEndDivider.visibility = View.VISIBLE
                endLy.visibility = View.VISIBLE
                durationText.text = getDurationText(startCal.timeInMillis, endCal.timeInMillis, !record.isSetTime())
            }
        }else {
            timeLy.visibility = View.GONE
        }
    }

    fun showStartEndDialog() {
        showDialog(SchedulingDialog(this, record) { sCal, eCal ->
            record.setSchedule()
            record.setDateTime(sCal, eCal)
            updateUI()
        }, true, true, true, false)
    }

    fun updateDdayUI() {
        if(record.isSetDday()) {
            ddayLy.visibility = View.VISIBLE
            ddayText.text = record.getDdayText(System.currentTimeMillis())
            ddayLy.setOnClickListener {
                showDialog(CustomDialog(this, getString(R.string.countdown),
                        getString(R.string.delete_dday_sub), null) { result, _, _ ->
                    if(result) {
                        record.clearDday()
                        updateDdayUI()
                    }
                }, true, true, true, false)
            }
        }else {
            ddayLy.visibility = View.GONE
        }
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
                    showDialog(CustomDialog(this, getString(R.string.location), null,
                            arrayOf(getString(R.string.delete), getString(R.string.edit))) { result, index, _ ->
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
            memoLy.visibility = View.GONE
        }else {
            memoInput.setText(record.description)
            memoLy.visibility = View.VISIBLE
        }
    }

    fun showMemoUI() {
        memoLy.visibility = View.VISIBLE
        callAfterViewDrawed(memoInput, Runnable{ showKeyPad(memoInput) })
    }

    fun updateLinkUI() {
        if(record.isSetLink()) {
            linkLy.visibility = View.VISIBLE
            linkListView.setList(record)
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
            if(originalData?.isRepeat() == true) {
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
        MainActivity.getTemplateView()?.collapseNoAnim()
        finish()
    }

    private fun deletedFinish() {
        toast(R.string.deleted, R.drawable.delete)
        MainActivity.getTemplateView()?.collapseNoAnim()
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
        showDialog(AlarmPickerDialog(this, record.getAlarmOffset(), record.getDtAlarm()) { result, offset, dtAlarm ->
            if (result) {
                record.setAlarm(offset, dtAlarm)
            } else {
                record.removeAlarm()
            }
            updateAlarmUI()
        }, true, true, true, false)
    }

    private fun showTimePicker(time: Long, onResult: (Long) -> (Unit)) {
        showDialog(TimePickerDialog(this, time, onResult),
                true, true, true, false)
    }

    fun showEditWebsiteDialog() {
        showDialog(AddWebLinkDialog(this) { link ->
            record.links.add(link)

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
                                    }.addOnSuccessListener { _ ->
                                        ref.downloadUrl.addOnCompleteListener {
                                            l("다운로드 url : ${it.result.toString()}")
                                            record.links.add(Link(imageId, Link.Type.IMAGE.ordinal,
                                                    null, it.result.toString()))
                                            hideProgressDialog()
                                            updateLinkUI()
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

    override fun onDestroy() {
        super.onDestroy()
        keypadListener?.unregister()
    }
}