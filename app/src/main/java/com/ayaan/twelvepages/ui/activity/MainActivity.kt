package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.FolderAdapter
import com.ayaan.twelvepages.listener.MainDragAndDropListener
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.CountdownListDialog
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import com.ayaan.twelvepages.ui.dialog.UndoneListDialog
import com.ayaan.twelvepages.ui.sheet.CalendarSettingsSheet
import com.ayaan.twelvepages.ui.sheet.DayViewSettingsSheet
import com.ayaan.twelvepages.ui.sheet.TemplateSheet
import com.ayaan.twelvepages.viewmodel.MainViewModel
import com.ayaan.twelvepages.widget.MonthlyCalendarWidget
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import com.theartofdev.edmodo.cropper.CropImage
import io.realm.RealmResults
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
        val tabSize = dpToPx(40)
        fun getViewModel() = instance?.viewModel
        fun getDayPager() = instance?.dayPager
        fun getMainPanel() = instance?.mainPanel
        fun getCalendarLy() = instance?.calendarLy
        fun getCalendarPager() = instance?.calendarPager
        fun getMainMonthText() = instance?.mainMonthText
        fun getFakeDateText() = instance?.fakeDateText
        fun getTargetTemplate() = getViewModel()?.targetTemplate?.value
        fun getTargetCalendarView() = getViewModel()?.targetCalendarView?.value
        fun getTargetTime() = getViewModel()?.targetTime?.value
        fun getTargetCal() = getViewModel()?.targetCalendarView?.value?.targetCal
        fun getTargetFolder() = getViewModel()?.targetFolder?.value ?: Folder()
        fun isFolderOpen() = getViewModel()?.openFolder?.value == true
        fun isProfileOpened() = instance?.profileView?.isOpened() == true
        fun closeProfileView() = instance?.profileView?.hide()
    }

    lateinit var viewModel: MainViewModel
    private var reservedIntentAction: Runnable? = null
    private val briefingHander = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            sendEmptyMessageDelayed(0, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        l("[MainActivity onCreate]")
        MobileAds.initialize(this, "ca-app-pub-6927758618180863~9253162897") /*개발 ca-app-pub-3940256099942544~3347511713*/ /*운영 ca-app-pub-6927758618180863~9253162897*/
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initTheme(rootLy)
        initMain()
        if(FirebaseAuth.getInstance().currentUser == null) {

        }else {
            viewModel.initRealm(SyncUser.current())
        }

        val ver = packageManager.getPackageInfo(App.context.packageName, 0).versionName
        if(Prefs.getString("last_patch_note_ver", "") != ver) {
            val dialog = CustomDialog(this@MainActivity, "$ver 패치노트",
                    """
                        1. 날짜에 배경색을 지정 할 수 있습니다.
                        
                        2. 친구에게 달의기록 앱을 공유하고 스티커 팩을 받아보세요! 좌측 하단 메뉴버튼을 클릭하시면 공유가 가능합니다.
                        
                        가이드
                        
                        1. 많은 분들이 일간 -> 월간 전환하는 방법에 대해서 문의를 주셨습니다.
                        버튼을 넣어볼까 했지만 아무래도 최소한의 버튼만 메인에 두는것이 좋다는 판단하에
                        하단 메뉴바의 빈 공간을 탭하는 방식으로 가능하도록 해두었습니다.
                        
                        2. 설정 -> 일간화면 및 목록 스타일 설정에서 이 날의 사진을 끌 수 있습니다.
                        
                        3. 날짜를 길게 눌러 드래그하면 긴 구간을 한번에 입력 할 수 있습니다.
                    """.trimIndent(), null, R.drawable.info) { result, _, _ ->
            }
            showDialog(dialog, true, true, true, false)
            dialog.hideCancelBtn()
            Prefs.putString("last_patch_note_ver", ver)
        }
    }

    private fun initMain() {
        initLayout()
        initBottomBar()
        initCalendarView()
        initFolderView()
        initDayView()
        initBtns()
        initObserver()
    }

    private fun playIntentAction() {
        if(viewModel.realm.value != null) {
            playAction(intent.getIntExtra("action", 0), intent.getBundleExtra("bundle"))
            intent.removeExtra("action")
        }else {
            reservedIntentAction = Runnable { playIntentAction() }
        }
    }

    fun playAction(action: Int, bundle: Bundle?) {
        rootLy.postDelayed({
            when(action) {
                2 -> {
                    bundle?.let {
                        viewModel.setTargetTimeObjectById(bundle.getString("recordId"),
                                bundle.getLong("dtStart", Long.MIN_VALUE))
                    }
                }
            }
        }, 50)
    }

    private fun initLayout() {
        rootLy.setOnDragListener(MainDragAndDropListener)
        mainPanel.setOnClickListener {}
        mainDateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        mainMonthText.pivotY = 0f
        mainMonthText.pivotX = 0f
        callAfterViewDrawed(rootLy, Runnable{})
    }

    private fun initBottomBar() {
        bottomBar.setOnClickListener {
            if(dayPager.viewMode != ViewMode.ANIMATING) {
                if(dayPager.isOpened()) dayPager.hide() else dayPager.show()
            }
        }
        addBtn.setOnClickListener { viewModel.targetTime.value?.let { showTemplateSheet(it, it) } }
    }

    private fun initCalendarView() {
        calendarPager.onSelectedDate = { calendarView, dateInfoHolder, openDayView ->
            viewModel.targetTime.value = dateInfoHolder.time
            viewModel.targetCalendarView.value = calendarView
            if(openDayView && dayPager.viewMode == ViewMode.CLOSED) dayPager.show()
            refreshTodayView(calendarView.todayStatus)
        }
    }

    private val folderAdapter = FolderAdapter(this, ArrayList())

    private fun initFolderView() {
        folderListView.layoutManager = LinearLayoutManager(this)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)
        folderBtn.setOnClickListener {
            viewModel.openFolder.value = viewModel.openFolder.value != true
        }
    }

    private fun initDayView() {
        dayPager.onVisibility = { show ->

        }
    }

    private fun initBtns() {
        profileBtn.setOnClickListener {
            if(!profileView.isOpened()) {
                profileView.show()
            }
        }

        searchBtn.setOnClickListener { showSearchView() }

        mainDateLy.setOnClickListener {
            showDialog(DatePickerDialog(this, viewModel.targetTime.value!!) {
                selectDate(it)
            }, true, true, true, false)
        }

        profileBtn.setOnLongClickListener {

val fontPath = "/system/fonts"
val fontFiles = File(fontPath)
val fontFileArray = fontFiles.listFiles()
var fontPathString = ""
            fontFileArray.forEach {
                fontPathString += it.toString()
                fontPathString += "\n"
            }
l("!!!!!!!font : $fontPathString")


//            val cal = Calendar.getInstance()
//            cal.set(2019, 9, 1)
//            var s = cal.timeInMillis
//            val list = ArrayList<Record>()
//            val c = 0
//            val formulas = arrayOf(RecordCalendarAdapter.Formula.SINGLE_TEXT, RecordCalendarAdapter.Formula.MULTI_TEXT, RecordCalendarAdapter.Formula.DOT)
//            list.add(RecordManager.makeNewRecord(s, s).apply {
//                title = "점심약속"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*2, s+DAY_MILL*3).apply {
//                title = "오후미팅"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*5, s+DAY_MILL*5).apply {
//                title = "치과"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s, s+DAY_MILL*3).apply {
//                title = "회사 프로젝트"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*7, s+DAY_MILL*7).apply {
//                title = "친구생일"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*7, s+DAY_MILL*7).apply {
//                title = "선물사기"
//                type = 2
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*11, s+DAY_MILL*17).apply {
//                title = "점심약속"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*22, s+DAY_MILL*23).apply {
//                title = "오후미팅"
//                type = 1
//                colorKey = c + Random().nextInt(10)val dialog = CustomDialog(this@MainActivity, "$ver 패치노트",
//                    """
//                        1. 날짜에 배경색을 지정 할 수 있습니다.
//
//                        2. 친구에게 달의기록 앱을 공유하고 스티커 팩을 받아보세요! 좌측 하단 메뉴버튼을 클릭하시면 공유가 가능합니다.
//
//                        가이드
//
//                        1. 많은 분들이 일간 -> 월간 전환하는 방법에 대해서 문의를 주셨습니다.
//                        버튼을 넣어볼까 했지만 아무래도 최소한의 버튼만 메인에 두는것이 좋다는 판단하에
//                        하단 메뉴바의 빈 공간을 탭하는 방식으로 가능하도록 해두었습니다.
//
//                        2. 설정 -> 일간화면 및 목록 스타일 설정에서 이 날의 사진을 끌 수 있습니다.
//
//                        3. 날짜를 길게 눌러 드래그하면 긴 구간을 한번에 입력 할 수 있습니다.
//                    """.trimIndent(), null, R.drawable.info) { result, _, _ ->
//            }
//            showDialog(dialog, true, true, true, false)
//            dialog.hideCancelBtn()
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*27, s+DAY_MILL*30).apply {
//                title = "헬스장"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*29, s+DAY_MILL*29).apply {
//                title = "대청소"
//                type = 1
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*10, s+DAY_MILL*10).apply {
//                title = "빨래하기"
//                isSetCheckBox = true
//                colorKey = c + Random().nextInt(10)
//            })
//            list.add(RecordManager.makeNewRecord(s+DAY_MILL*25, s+DAY_MILL*25).apply {
//                title = "택배받기"
//                isSetCheckBox = true
//                colorKey = c + Random().nextInt(10)
//            })
//            list.forEach {
//                //val f = formulas[Random().nextInt(formulas.size)]
//                val f = RecordCalendarAdapter.Formula.SINGLE_TEXT
//                //it.colorKey = 9 // 검정
//                //it.style = f.shapes[Random().nextInt(f.shapes.size)].ordinal * 100 + f.ordinal
//                it.style = f.shapes[0].ordinal * 100 + f.ordinal
//            }
//            RecordManager.save(list)
            return@setOnLongClickListener true
        }
    }

    private fun initObserver() {
        viewModel.realm.observe(this, Observer { realm ->
            if(realm != null) {
                reservedIntentAction?.run()
                reservedIntentAction = null
            }
        })
        viewModel.loading.observe(this, Observer {
            if(it as Boolean) {}
        })
        viewModel.targetRecord.observe(this, Observer { timeObject ->
            if(timeObject != null) {
                startActivity(Intent(this@MainActivity, RecordActivity::class.java))
            }
        })
        viewModel.appUser.observe(this, Observer { appUser -> appUser?.let { profileView.updateUserUI(it) } })
        viewModel.templateList.observe(this, Observer { clipboardView.notifyListChanged() })
        viewModel.folderList.observe(this, Observer { list ->
            //folderAdapter.refresh(list)
        })
        viewModel.targetFolder.observe(this, Observer { folder ->
            refreshAll()
            //folder?.let { folderAdapter.setTargetFolder(it, folderListView, if(folder.type == 0) calendarLy else noteView) }
        })
        viewModel.openFolder.observe(this, Observer { updateFolderUI(it) })
        viewModel.targetCalendarView.observe(this, Observer { setDateText() })
        viewModel.clipRecord.observe(this, Observer { clipboardView.clip(it) })
        viewModel.countdownRecords.observe(this, Observer { updateCountdownUI(it) })
        viewModel.undoneRecords.observe(this, Observer { updateUndoneUI(it) })
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountdownUI(list: RealmResults<Record>?) {
        if(list.isNullOrEmpty() || System.currentTimeMillis() < Prefs.getLong("briefingCountdownTime", 0)) {
            countdownBtn.visibility = View.GONE
        }else {
            countdownBtn.visibility = View.VISIBLE
            list[0]?.let { record ->
                var tail = if (list.size > 1) " " + String.format(str(R.string.and_others), list.size - 1) else ""
                if (tail.contains("other") && list.size > 2) tail = tail.replace("other", "others")
                countdownText.text = "${record.getShortTilte()} ${record.getCountdownText(System.currentTimeMillis())}$tail"
                countdownBtn.setOnClickListener {
                    showDialog(CountdownListDialog(this) {
                        Prefs.putLong("briefingCountdownTime", getTodayStartTime() + DAY_MILL)
                        countdownBtn.visibility = View.GONE
                    }, true, true, true, false)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUndoneUI(list: RealmResults<Record>?) {
        if(list.isNullOrEmpty() || System.currentTimeMillis() < Prefs.getLong("briefingUndoneTime", 0)) {
            undoneBtn.visibility = View.GONE
        }else {
            undoneBtn.visibility = View.VISIBLE
            list[0]?.let { record ->
                var tail = if (list.size > 1) " "+ String.format(str(R.string.and_others), list.size - 1) else ""
                if (tail.contains("other") && list.size > 2) tail = tail.replace("other", "others")
                undoneText.text = "${str(R.string.undone_records)} ${record.getShortTilte()}$tail"
                undoneBtn.setOnClickListener {
                    showDialog(UndoneListDialog(this) {
                        Prefs.putLong("briefingUndoneTime", getTodayStartTime() + DAY_MILL)
                        undoneBtn.visibility = View.GONE
                    }, true, true, true, false)
                }
            }
        }
    }

    private fun updateFolderUI(isOpen: Boolean) {
        TransitionManager.beginDelayedTransition(mainPanel, makeChangeBounceTransition())
        val animSet = AnimatorSet()
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        if(isOpen) {
            (folderListView.layoutParams as FrameLayout.LayoutParams).leftMargin = 0
            (contentLy.layoutParams as FrameLayout.LayoutParams).let {
                it.rightMargin = -tabSize
                it.leftMargin = tabSize
            }
            (folderBtn.layoutParams as FrameLayout.LayoutParams).let {
                it.width = dpToPx(60)
                it.leftMargin = dpToPx(10)
            }
            animSet.playTogether(ObjectAnimator.ofFloat(folderArrowImg, "rotation", 0f, 180f),
                    ObjectAnimator.ofFloat(folderArrowImg, "translationX", 0f, -dpToPx(13f)))
        }else {
            (folderListView.layoutParams as FrameLayout.LayoutParams).leftMargin = -tabSize
            (contentLy.layoutParams as FrameLayout.LayoutParams).let {
                it.rightMargin = 0
                it.leftMargin = 0
            }
            (folderBtn.layoutParams as FrameLayout.LayoutParams).let {
                it.width = dpToPx(75)
                it.leftMargin = -dpToPx(40)
            }
            animSet.playTogether(ObjectAnimator.ofFloat(folderArrowImg, "rotation", 180f, 0f),
                    ObjectAnimator.ofFloat(folderArrowImg, "translationX", -dpToPx(13f), 0f))
        }
        animSet.start()
        mainPanel.requestLayout()
    }

    private fun refreshAll() {
        l("[Main Refresh All]")
        val folder = getTargetFolder()
        if(folder.type == 0) {
            calendarLy.visibility = View.VISIBLE
            noteView.visibility = View.INVISIBLE
            refreshCalendar()
        }else {
            calendarLy.visibility = View.INVISIBLE
            noteView.visibility = View.VISIBLE
            noteView.notifyDataChanged()
        }
        clipboardView.notifyListChanged()
    }

    private fun refreshCalendar() {
        calendarPager.redraw()
        calendarPager.selectDate(viewModel.targetTime.value ?: System.currentTimeMillis())
        if(dayPager.isOpened()){
            dayPager.notifyDateChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        getTargetCal()?.let {
            fakeDateText.typeface = AppTheme.dateFont
            mainMonthText.typeface = AppTheme.boldFont
            fakeDateText.text = String.format("%02d", it.get(Calendar.DATE))
            mainMonthText.setTextColor(CalendarManager.selectedDateColor)
            if(it.get(Calendar.YEAR) == getCurrentYear()) {
                mainMonthText.text = AppDateFormat.month.format(it.time)
            }else {
                mainMonthText.text = AppDateFormat.ym.format(it.time)
            }
        }
    }

    fun selectDate(time: Long) {
        calendarPager.selectDate(time)
        if(dayPager.isOpened()){
            dayPager.initTime(time)
            dayPager.redraw()
        }
    }

    private fun showSearchView() { searchView.show() }

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    private fun refreshTodayView(todayOffset: Int) {
        when {
            todayOffset != 0 -> {
                //var distance = Math.min(Math.abs(todayOffset / 7 * dpToPx(2f)), dpToPx(150f))
                var distance = 0f
                if(todayOffset < 0) {
                    distance *= -1
                    (todayBtn.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.BOTTOM
                    todayText.setPadding(dpToPx(5), 0, 0, 0)
                    todayRightArrow.visibility = View.VISIBLE
                    todayLeftArrow.visibility = View.GONE
                }else {
                    (todayBtn.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.BOTTOM
                    todayText.setPadding(0, 0, dpToPx(5), 0)
                    todayRightArrow.visibility = View.GONE
                    todayLeftArrow.visibility = View.VISIBLE
                }
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayCard, "alpha",  todayCard.alpha, 1f),
                        ObjectAnimator.ofFloat(todayBtn, "translationX",  todayBtn.translationX, distance))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener {
                    selectDate(getTodayStartTime())
                }
                todayBtn.isEnabled = true
            }
            else -> {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayCard, "alpha",  todayCard.alpha, 0f),
                        ObjectAnimator.ofFloat(todayBtn, "translationX", todayBtn.translationX, 0f))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener(null)
                todayBtn.isEnabled = false
                /*
                todayBtn.setOnClickListener { _ ->
                    MainActivity.getdayPager()?.let {
                        if(it.isOpened()) {
                            it.hide()
                        }else if(it.viewMode == ViewMode.CLOSED){
                            it.show()
                        }
                    }
                }
                */
            }
        }
    }

    fun deliveryDragEvent(event: DragEvent) {
        if(event.y > calendarPager.top && event.y < calendarPager.bottom) {
            calendarPager.onDrag(event)
        }
    }

    fun endDrag() { calendarPager.endDrag() }
    fun clearCalendarHighlight() { calendarPager.clearHighlight() }

    fun checkExternalStoragePermission(requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        } else {
            showPhotoPicker(requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PROFILE_IMAGE -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showPhotoPicker(requestCode) }
                return
            }
            RC_PHOTO_ON_DAYVIEW -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ ->
                            AppStatus.rememberPhoto = YES
                            AppStatus.permissionStorage = true
                            Prefs.putInt("rememberPhoto", AppStatus.rememberPhoto)
                            dayPager.redraw()
                        }
                return
            }
            RC_IMAGE_ATTACHMENT -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> supportFragmentManager.fragments.forEach { it?.onRequestPermissionsResult(requestCode, permissions, grantResults) } }
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

    fun showTemplateSheet(dtStart: Long, dtEnd: Long) {
        TemplateSheet(dtStart, dtEnd).show(supportFragmentManager, null)
    }

    override fun onBackPressed() {
        when{
            searchView.isOpened() -> searchView.hide()
            profileView.isOpened() -> profileView.hide()
            clipboardView.isExpanded() -> clipboardView.collapse()
            viewModel.openFolder.value == true -> viewModel.openFolder.value = false
            dayPager.isOpened() -> dayPager.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PROFILE_IMAGE && resultCode == RESULT_OK) {
            data?.let { CropImage.activity(data.data).setAspectRatio(3, 4).start(this) }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                showProgressDialog(null)
                Glide.with(this).asBitmap().load(uri)
                        .into(object : SimpleTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                l("사진 크기 : ${resource.rowBytes} 바이트")
                                val ref = FirebaseStorage.getInstance().reference
                                        .child("${viewModel.appUser.value?.id}/profileImg.jpg")
                                val baos = ByteArrayOutputStream()
                                resource.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                                val uploadTask = ref.putBytes(baos.toByteArray())
                                uploadTask.addOnFailureListener {
                                    hideProgressDialog()
                                }.addOnSuccessListener {
                                    ref.downloadUrl.addOnCompleteListener {
                                        l("다운로드 url : ${it.result.toString()}")
                                        val user = FirebaseAuth.getInstance().currentUser
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setPhotoUri(it.result)
                                                .build()
                                        user?.updateProfile(profileUpdates)
                                                ?.addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        profileView.updateUserUI(viewModel.appUser.value!!)
                                                    }
                                                    hideProgressDialog()
                                                }
                                    }
                                }
                            }
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                hideProgressDialog()
                            }
                        })
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error.printStackTrace()
            }
        }else if(requestCode == RC_OS_CALENDAR) {
            refreshCalendar()
        }else if(requestCode == RC_SETTING && resultCode == RESULT_CALENDAR_SETTING) {
            if(!getTargetFolder().isCalendar()) viewModel.setCalendarFolder()
            if(viewModel.openFolder.value == true) viewModel.openFolder.value = false
            if(dayPager.isOpened()) dayPager.hide()
            profileView.hide()
            CalendarSettingsSheet(this).show(supportFragmentManager, null)
        }else if(requestCode == RC_SETTING && resultCode == RESULT_DAYVIEW_SETTING) {
            if(!getTargetFolder().isCalendar()) viewModel.setCalendarFolder()
            if(viewModel.openFolder.value == true) viewModel.openFolder.value = false
            if(dayPager.isClosed()) dayPager.show()
            profileView.hide()
            DayViewSettingsSheet(this).show(supportFragmentManager, null)
        }else if(requestCode == RC_SETTING && resultCode == RC_LOGOUT) {
            finish()
            startActivity(Intent(this, WelcomeActivity::class.java))
        }else if(requestCode == RC_APP_SHARE) {
            Prefs.putBoolean("isTakeShareGift", false)
            l("!!!!!!")

            val dialog = CustomDialog(this@MainActivity, "감사합니다!",
                    """
                        친구에게 잘
                        스티커 팩이 지급되었습니다.
                    """.trimIndent(), null, R.drawable.info) { result, _, _ ->
            }
            showDialog(dialog, true, true, true, false)
            dialog.hideCancelBtn()
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        playIntentAction()
    }

    override fun onStart() {
        super.onStart()
        AppStatus.permissionStorage = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStop() {
        super.onStop()
        isShowing = false
        val intent = Intent(this, MonthlyCalendarWidget::class.java)
        intent.action = "android.appwidget.action.APPWIDGET_UPDATE"
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val lastBackupTime = Prefs.getLong("last_backup_time", 0L)
        if(RecordManager.isChanged && (AppStatus.isPremium() || lastBackupTime < System.currentTimeMillis() - DAY_MILL * 7)) {
            RecordManager.isChanged = false
            backupDB(null, null)
        }
    }

    fun setPremium() {
        viewModel.appUser.value?.let { profileView.updateUserUI(it) }
    }
}