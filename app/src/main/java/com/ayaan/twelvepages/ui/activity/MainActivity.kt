package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.FolderAdapter
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*
import com.ayaan.twelvepages.listener.MainDragAndDropListener
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.CalendarSettingsDialog
import com.ayaan.twelvepages.ui.dialog.CountdownListDialog
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import com.ayaan.twelvepages.ui.dialog.UndoneListDialog
import com.ayaan.twelvepages.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import io.realm.RealmResults
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
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
        fun getDowLy() = instance?.dowLy
        fun getCalendarPager() = instance?.calendarPager
        fun getMainDateLy() = instance?.mainDateLy
        fun getMainYearText() = instance?.mainYearText
        fun getProfileBtn() = instance?.profileBtn
        fun getTemplateView() = instance?.templateView
        fun getTargetTemplate() = getViewModel()?.targetTemplate?.value
        fun getTargetCalendarView() = getViewModel()?.targetCalendarView?.value
        fun getTargetTime() = getViewModel()?.targetTime?.value
        fun getTargetCal() = getViewModel()?.targetCalendarView?.value?.targetCal
        fun getTargetFolder() = getViewModel()?.targetFolder?.value ?: Folder()
        fun isFolderOpen() = getViewModel()?.openFolder?.value == true
    }

    lateinit var viewModel: MainViewModel
    private var reservedIntentAction: Runnable? = null
    private val briefingHander = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            sendEmptyMessageDelayed(0, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initTheme(rootLy)
        initMain()
        viewModel.initRealm(SyncUser.current())
        if(FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(Intent(this, WelcomeActivity::class.java), RC_LOGIN)
        }else {
            //startActivityForResult(Intent(this, WelcomeActivity::class.java), RC_LOGIN)
        }
        /*
        if(SyncUser.current() == null) {
            startActivityForResult(Intent(this, WelcomeActivity::class.java), RC_LOGIN)
        }else {
            viewModel.initRealm(SyncUser.current())
        }*/
    }

    private fun initMain() {
        initLayout()
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
        when(action) {
            2 -> {
                bundle?.let {
                    viewModel.setTargetTimeObjectById(bundle.getString("recordId"),
                            bundle.getLong("dtStart", Long.MIN_VALUE))
                }
            }
        }
    }

    private fun initLayout() {
        rootLy.setOnDragListener(MainDragAndDropListener)
        mainDateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        mainDateLy.pivotX = dpToPx(10f)
        mainDateLy.pivotY = dpToPx(0f)
        briefingCard.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        mainPanel.setOnClickListener {}
        todayBtn.translationY = tabSize.toFloat()
        callAfterViewDrawed(rootLy, Runnable{
            /*
            val rectangle = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight = rectangle.top
            val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).getTop()
            val titleBarHeight = contentViewTop - statusBarHeight
            l("StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight)
            */
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppStatus.statusBarHeight = location[1]
        })
    }

    private fun initCalendarView() {
        calendarPager.onSelectedDate = { calendarView, dateInfoHolder, openDayView ->
            viewModel.targetTime.value = dateInfoHolder.time
            viewModel.targetCalendarView.value = calendarView

            val dowTexts = arrayOf(dowText0, dowText1, dowText2, dowText3, dowText4, dowText5, dowText6)
            dowTexts.forEachIndexed { index, textView ->
                textView?.text = calendarView.dateCellHolders[index].getDowText()
                if(dateInfoHolder.cellNum % 7 == index) {
                    textView?.setTypeface(AppTheme.boldFont, Typeface.BOLD)
                }else {
                    textView?.typeface = AppTheme.regularFont
                }
                textView?.setTextColor(when (index) {
                    calendarView.sundayPos -> CalendarManager.sundayColor
                    calendarView.saturdayPos -> CalendarManager.saturdayColor
                    else -> {
                        if(dateInfoHolder.cellNum % 7 == index) {
                            CalendarManager.selectedDateColor
                        }else {
                            CalendarManager.dateColor
                        }
                    }
                })
            }

            if(openDayView && dayPager.viewMode == ViewMode.CLOSED) dayPager.show()
            refreshTodayView(calendarView.todayStatus)
        }
        calendarPager.onTop = { isTop, isBottom ->
            if(isTop){
                if(topShadow.alpha != 0f){
                    ObjectAnimator.ofFloat(topShadow, "alpha", topShadow.alpha, 0f).start()
                }
            } else {
                if(topShadow.alpha != 1f) {
                    ObjectAnimator.ofFloat(topShadow, "alpha", topShadow.alpha, 1f).start()
                }
            }
        }
    }

    private val folderAdapter = FolderAdapter(this, ArrayList())

    private fun initFolderView() {
        folderListView.layoutManager = LinearLayoutManager(this)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)
        folderBtn.setOnClickListener {
            vibrate(this)
            viewModel.openFolder.value = viewModel.openFolder.value != true
        }
    }

    private fun initDayView() {
        dayPager.onVisibility = { show -> }
    }

    private fun initBtns() {
        profileBtn.setOnClickListener {
            if(!profileView.isOpened()) {
                profileView.show()
            }
        }

        profileBtn.setOnLongClickListener {
            RecordManager.deleteAllRecord()
            return@setOnLongClickListener true
        }

        mainYearText.setOnClickListener {
            if(!profileView.isOpened()) {
                profileView.show()
            }
        }

        mainYearText.setOnLongClickListener {
            AppTheme.thinFont = ResourcesCompat.getFont(this, R.font.thin_s)!!
            AppTheme.regularFont = ResourcesCompat.getFont(this, R.font.regular_s)!!
            AppTheme.boldFont = ResourcesCompat.getFont(this, R.font.bold_s)!!
            initTheme(rootLy)
            return@setOnLongClickListener true
        }

        mainMonthText.setOnClickListener {
            showDialog(DatePickerDialog(this, viewModel.targetTime.value!!) {
                selectDate(it)
            }, true, true, true, false)
        }

        mainMonthText.setOnLongClickListener {
            val cal = Calendar.getInstance()
            cal.set(2019, 7, 1)
            var s = cal.timeInMillis
            val list = ArrayList<Record>()
            val c = 0
            val formulas = arrayOf(STACK, EXPANDED, DOT)
            list.add(RecordManager.makeNewRecord(s, s).apply {
                title = "점심약속"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*2, s+DAY_MILL*3).apply {
                title = "오후미팅"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*5, s+DAY_MILL*5).apply {
                title = "치과"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s, s+DAY_MILL*3).apply {
                title = "회사 프로젝트"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*7, s+DAY_MILL*7).apply {
                title = "친구생일"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*7, s+DAY_MILL*7).apply {
                title = "선물사기"
                type = 2
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*11, s+DAY_MILL*17).apply {
                title = "점심약속"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*22, s+DAY_MILL*23).apply {
                title = "오후미팅"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*27, s+DAY_MILL*30).apply {
                title = "헬스장"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*29, s+DAY_MILL*29).apply {
                title = "대청소"
                type = 1
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*10, s+DAY_MILL*10).apply {
                title = "빨래하기"
                isSetCheckBox = true
                colorKey = c + Random().nextInt(10)
            })
            list.add(RecordManager.makeNewRecord(s+DAY_MILL*25, s+DAY_MILL*25).apply {
                title = "택배받기"
                isSetCheckBox = true
                colorKey = c + Random().nextInt(10)
            })
            list.forEach {
                //val f = formulas[Random().nextInt(formulas.size)]
                val f = RecordCalendarAdapter.Formula.STACK
                it.colorKey = 9
                //it.style = f.shapes[Random().nextInt(f.shapes.size)].ordinal * 100 + f.ordinal
                it.style = f.shapes[1].ordinal * 100 + f.ordinal
            }
            RecordManager.save(list)
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
        viewModel.appUser.observe(this, Observer { appUser -> appUser?.let { updateUserUI(it) } })
        viewModel.templateList.observe(this, Observer { templateView.notifyListChanged() })
        viewModel.folderList.observe(this, Observer { list -> folderAdapter.refresh(list) })
        viewModel.targetFolder.observe(this, Observer { folder ->
            refreshAll()
            folder?.let { folderAdapter.setTargetFolder(it, folderListView, if(folder.type == 0) calendarLy else noteView) }
        })
        viewModel.openFolder.observe(this, Observer { updateFolderUI(it) })
        viewModel.targetCalendarView.observe(this, Observer { setDateText() })
        viewModel.clipRecord.observe(this, Observer { updateClipUI(it) })
        viewModel.countdownRecords.observe(this, Observer { updateCountdownUI(it) })
        viewModel.undoneRecords.observe(this, Observer { updateUndoneUI(it) })
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountdownUI(list: RealmResults<Record>?) {
        if(list.isNullOrEmpty()) {
            countdownBtn.visibility = View.GONE
        }else {
            countdownBtn.visibility = View.VISIBLE
            list[0]?.let { record ->
                val color = record.getColor()
                val fontColor = ColorManager.getFontColor(color)
                countdownText.text = record.getCountdownText(System.currentTimeMillis())
                //countdownCard.setCardBackgroundColor(color)
                countdownImg.setColorFilter(AppTheme.primaryText)
                countdownText.setTextColor(AppTheme.primaryText)
                countdownText.setOnClickListener {
                    showDialog(CountdownListDialog(this) {
                    }, true, true, true, false)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUndoneUI(list: RealmResults<Record>?) {
        if(list.isNullOrEmpty()) {
            undoneBtn.visibility = View.GONE
        }else {
            undoneBtn.visibility = View.VISIBLE
            list[0]?.let { record ->
                val color = record.getColor()
                val fontColor = ColorManager.getFontColor(color)
                //undoneCard.setCardBackgroundColor(color)
                //undoneImg.setColorFilter(fontColor)
                //undoneText.setTextColor(fontColor)

                var tail = if (list.size > 1) String.format(str(R.string.and_others), list.size - 1) else ""
                if (tail.contains("other") && list.size > 2) {
                    tail = tail.replace("other", "others")
                }
                undoneText.text = "${str(R.string.undone_records)}\n${record.getShortTilte()} $tail"
                undoneText.setOnClickListener {
                    showDialog(UndoneListDialog(this) {
                    }, true, true, true, false)
                }
            }
        }
    }

    private fun updateClipUI(record: Record?) {
        TransitionManager.beginDelayedTransition(clipView, makeFromBottomSlideTransition())
        if(record == null) {
            clipView.visibility = View.INVISIBLE
        }else {
            clipView.visibility = View.VISIBLE
            if(record.id.isNullOrEmpty()) {
                clipTypeText.text = str(R.string.copy)
            }else {
                clipTypeText.text = str(R.string.cut)
            }
            clipText.text = record.getTitleInCalendar()
            clipIconImg.setColorFilter(record.getColor())
            clipPasteBtn.setOnClickListener {
                viewModel.targetFolder.value?.let { record.folder = Folder(it) }
                record.setDate(viewModel.targetTime.value ?: Long.MIN_VALUE)
                if(record.id.isNullOrEmpty()) {
                    if(record.isRepeat()) {
                        record.clearRepeat()
                    }
                    RecordManager.save(record)
                    toast(R.string.copied, R.drawable.copy)
                }else {
                    if(record.isRepeat()) {
                        RecordManager.deleteOnly(record)
                        record.clearRepeat()
                        record.id = null
                        RecordManager.save(record)
                    }else {
                        RecordManager.delete(record)
                        record.id = null
                        RecordManager.save(record)
                    }
                    toast(R.string.moved, R.drawable.change)
                }
                viewModel.clipRecord.value = null
            }
            clipCloseBtn.setOnClickListener {
                viewModel.clipRecord.value = null
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
                    ObjectAnimator.ofFloat(folderArrowImg, "translationX", 0f, -dpToPx(13f)),
                    ObjectAnimator.ofFloat(profileBtn, "translationX", 0f, tabSize.toFloat()))
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
                    ObjectAnimator.ofFloat(folderArrowImg, "translationX", -dpToPx(13f), 0f),
                    ObjectAnimator.ofFloat(profileBtn, "translationX", tabSize.toFloat(), 0f))
        }
        animSet.start()
        mainPanel.requestLayout()
    }

    private fun refreshAll() {
        val folder = getTargetFolder()
        if(folder.type == 0) {
            calendarLy.visibility = View.VISIBLE
            todayBtn.visibility = View.VISIBLE
            noteView.visibility = View.INVISIBLE
        }else {
            calendarLy.visibility = View.INVISIBLE
            todayBtn.visibility = View.INVISIBLE
            noteView.visibility = View.VISIBLE
        }
        refreshCalendar()
        noteView.notifyDataChanged()
    }

    private fun refreshCalendar() {
        calendarPager.redraw()
        if(dayPager.isOpened()){
            dayPager.notifyDateChanged()
        }
    }

    private fun updateUserUI(appUser: AppUser) {
        l("[프로필 갱신]")
        updateProfileImage()
        profileView.updateUserUI(appUser)
    }

    private fun updateProfileImage() {
        when {
            FirebaseAuth.getInstance().currentUser?.photoUrl != null ->
                Glide.with(this).load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                    .apply(RequestOptions().override(dpToPx(150)))
                    .into(profileImg)
            else -> profileImg.setImageResource(R.drawable.profile)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        getTargetCal()?.let {
            mainMonthText.setTextColor(AppTheme.primaryText)
            mainYearText.setTextColor(AppTheme.primaryText)
            //mainYearText.text = AppDateFormat.year.format(it.time)
            mainYearText.text = "${it.get(Calendar.YEAR)}"
            //mainMonthText.text = String.format("%01d", (it.get(Calendar.MONTH) + 1))
            mainMonthText.text = AppDateFormat.monthEng.format(it.time)
        }
    }

    fun selectDate(time: Long) {
        calendarPager.selectDate(time)
        if(dayPager.isOpened()){
            dayPager.initTime(time)
            dayPager.notifyDateChanged()
        }
    }

    fun showSearchView() { searchView.show() }

    private fun refreshTodayView(todayOffset: Int) {
        when {
            todayOffset != 0 -> {
                todayBtn.visibility = View.VISIBLE
                //var distance = Math.min(Math.abs(todayOffset / 7 * dpToPx(2f)), dpToPx(150f))
                var distance = 0f
                if(todayOffset < 0) {
                    distance *= -1
                    todayText.setPadding(dpToPx(8), 0, 0, 0)
                    todayRightArrow.visibility = View.VISIBLE
                    todayLeftArrow.visibility = View.GONE
                }else {
                    todayText.setPadding(0, 0, dpToPx(8), 0)
                    todayRightArrow.visibility = View.GONE
                    todayLeftArrow.visibility = View.VISIBLE
                }
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayBtn, "translationY",  todayBtn.translationY, 0f),
                        ObjectAnimator.ofFloat(todayBtn, "translationX",  todayBtn.translationX, distance))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener { selectDate(System.currentTimeMillis()) }
            }
            else -> {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayBtn, "translationY", todayBtn.translationY, tabSize * 2f),
                        ObjectAnimator.ofFloat(todayBtn, "translationX", todayBtn.translationX, 0f))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener(null)
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
            RC_PRFOFILE_IMAGE -> {
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

    fun expandControlView(dtStart: Long, dtEnd: Long) {
        templateView.expand(dtStart, dtEnd)
    }

    override fun onBackPressed() {
        when{
            searchView.isOpened() -> searchView.hide()
            profileView.isOpened() -> profileView.hide()
            templateView.isExpanded -> templateView.collapse()
            viewModel.openFolder.value == true -> viewModel.openFolder.value = false
            dayPager.isOpened() -> dayPager.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_LOGIN) {
            if(resultCode == RESULT_OK) {

                viewModel.initRealm(SyncUser.current())
            } else finish()
        }else if (requestCode == RC_PRFOFILE_IMAGE && resultCode == RESULT_OK) {
            data?.let { CropImage.activity(data.data).setAspectRatio(1, 1).start(this) }
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
                                                        updateProfileImage()
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
            CalendarSettingsDialog(this).show(supportFragmentManager, null)
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        playIntentAction()
        //briefingHander.sendEmptyMessage(0)
    }

    override fun onPause() {
        super.onPause()
        //briefingHander.removeMessages(0)
    }

    override fun onStop() {
        super.onStop()
        isShowing = false
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}