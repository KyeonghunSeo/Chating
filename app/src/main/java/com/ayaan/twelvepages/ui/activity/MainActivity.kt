package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.FolderAdapter
import com.ayaan.twelvepages.listener.MainDragAndDropListener
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.ui.dialog.CalendarSettingsDialog
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import com.ayaan.twelvepages.ui.dialog.EditFolderDialog
import com.ayaan.twelvepages.viewmodel.MainViewModel
import com.theartofdev.edmodo.cropper.CropImage
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
        val tabSize = dpToPx(60)
        fun getViewModel() = instance?.viewModel
        fun getDayPagerView() = instance?.dayPagerView
        fun getMainPanel() = instance?.mainPanel
        fun getCalendarPagerView() = instance?.calendarPagerView
        fun getMainDateLy() = instance?.mainDateLy
        fun getMainMonthText() = instance?.mainMonthYearLy
        fun getWeekText() = instance?.weekText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    viewModel.setTargetTimeObjectById(bundle.getString("recordId"))
                }
            }
        }
    }

    private fun initLayout() {
        rootLy.setOnDragListener(MainDragAndDropListener)
        mainMonthYearLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        mainDateLy.pivotX = 0f
        mainDateLy.pivotY = 0f
        mainPanel.setOnClickListener {}
        todayBtn.translationY = tabSize.toFloat()
        callAfterViewDrawed(rootLy, Runnable{
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppStatus.statusBarHeight = location[1]
        })
    }

    private fun initCalendarView() {
        calendarPagerView.onSelectedDate = { time, cellNum, dateColor, isSameSeleted, calendarView ->
            viewModel.targetTime.value = time
            viewModel.targetCalendarView.value = calendarView
            monthText.setTextColor(dateColor)
            yearText.setTextColor(dateColor)
            weekText.setTextColor(dateColor)
            if(cellNum >= 0) {
                if(isSameSeleted && dayPagerView.viewMode == ViewMode.CLOSED) dayPagerView.show()
                refreshTodayView(calendarView.todayStatus)

            }
        }
        calendarPagerView.onTop = { isTop, isBottom ->
            if(isTop) topShadow.visibility = View.GONE
            else topShadow.visibility = View.VISIBLE
        }
    }

    private val folderAdapter = FolderAdapter(this, ArrayList()) { action, folder ->
        when(action) {
            0 -> {
                if(viewModel.targetFolder.value == folder) {
                    val dialog = EditFolderDialog(this@MainActivity, folder) { result ->
                        if(result) {

                        }else { // deleted
                            viewModel.setTargetFolder()
                        }
                    }
                    showDialog(dialog, true, true, true, false)
                }else {
                    viewModel.setTargetFolder(folder)
                }
            }
            1 -> {
                val dialog = EditFolderDialog(this@MainActivity, folder) { result ->
                    if(result) {
                        folderListView.post { folderListView.smoothScrollToPosition(
                                viewModel.folderList.value?.size ?: 0) }
                    }
                }
                showDialog(dialog, true, true, true, false)
            }
        }
    }

    private fun initFolderView() {
        folderListView.layoutManager = LinearLayoutManager(this)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)
        folderBtn.setOnClickListener {
            vibrate(this)
            //folderAdapter.notifyDataSetChanged()
            viewModel.openFolder.value = viewModel.openFolder.value != true
        }
    }

    private fun initDayView() {
        dayPagerView.onVisibility = { show -> }
    }

    private fun initBtns() {
        profileBtn.setOnClickListener {
            if(!profileView.isOpened()) {
                profileView.show()
            }
        }

        profileBtn.setOnLongClickListener {
            AppTheme.thinFont = ResourcesCompat.getFont(this, R.font.thin_s)!!
            AppTheme.regularFont = ResourcesCompat.getFont(this, R.font.regular_s)!!
            AppTheme.boldFont = ResourcesCompat.getFont(this, R.font.bold_s)!!
            initTheme(rootLy)
            return@setOnLongClickListener true
        }

        mainDateLy.setOnClickListener {
            showDialog(DatePickerDialog(this, viewModel.targetTime.value!!) {
                selectDate(it)
            }, true, true, true, false)
        }

        mainDateLy.setOnLongClickListener {
            val cal = Calendar.getInstance()
            cal.set(2019, 4, 1)
            val s = cal.timeInMillis
            RecordManager.save(RecordManager.makeNewRecord(s, s).apply {
                title = "점심약속"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s, s).apply {
                title = "오후미팅"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s+DAY_MILL, s+DAY_MILL).apply {
                title = "헬스장"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s, s+DAY_MILL*3).apply {
                title = "회사 프로젝트"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s+DAY_MILL*28, s+DAY_MILL*28).apply {
                title = "점심약속"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s+DAY_MILL*28, s+DAY_MILL*29).apply {
                title = "오후미팅"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s+DAY_MILL*28, s+DAY_MILL*30).apply {
                title = "헬스장"
                type = 1
            })
            RecordManager.save(RecordManager.makeNewRecord(s+DAY_MILL*29, s+DAY_MILL*29).apply {
                title = "회사 프로젝트"
                type = 1
            })
            return@setOnLongClickListener false
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
            folder?.let { folderAdapter.setTargetFolder(it, folderListView.layoutManager as LinearLayoutManager,
                        if(folder.type == 0) calendarLy else noteView) }
        })
        viewModel.openFolder.observe(this, Observer { updateFolderUI(it) })
        viewModel.targetCalendarView.observe(this, Observer { setDateText() })
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
                it.leftMargin = dpToPx(0)
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
                it.width = dpToPx(80)
                it.leftMargin = -dpToPx(40)
            }
            animSet.playTogether(ObjectAnimator.ofFloat(folderArrowImg, "rotation", 180f, 0f),
                    ObjectAnimator.ofFloat(folderArrowImg, "translationX", -dpToPx(13f), 0f))
        }
        animSet.start()
        mainPanel.requestLayout()
    }

    private fun refreshAll() {
        l("[메인 새로고침]")
        val folder = getTargetFolder()
        if(folder.type == 0) {
            calendarLy.visibility = View.VISIBLE
            noteView.visibility = View.INVISIBLE
        }else {
            calendarLy.visibility = View.INVISIBLE
            noteView.visibility = View.VISIBLE
        }
        refreshCalendar()
        noteView.notifyDataChanged()
    }

    private fun refreshCalendar() {
        calendarPagerView.redraw()
        if(dayPagerView.isOpened()){
            dayPagerView.notifyDateChanged()
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
                    .apply(RequestOptions().override(dpToPx(90)))
                    .into(profileImg)
            else -> profileImg.setImageResource(R.drawable.profile)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        getTargetCal()?.let {
            monthText.text = AppDateFormat.monthEng.format(it.time).toUpperCase()
            yearText.text = it.get(Calendar.YEAR).toString()
            weekText.text = String.format(getString(R.string.weekNum), it.get(Calendar.WEEK_OF_YEAR))
        }
    }

    fun selectDate(time: Long) {
        calendarPagerView.selectDate(time)
        if(dayPagerView.isOpened()){
            dayPagerView.initTime(time)
            dayPagerView.notifyDateChanged()
        }
    }

    fun showSearchView() { searchView.show() }

    private fun refreshTodayView(todayOffset: Int) {
        when {
            todayOffset != 0 -> {
                todayBtn.visibility = View.VISIBLE
                if(todayOffset < 0) {
                    todayText.setPadding(dpToPx(8), 0, 0, 0)
                    todayRightArrow.visibility = View.VISIBLE
                    todayLeftArrow.visibility = View.GONE
                }else {
                    todayText.setPadding(0, 0, dpToPx(8), 0)
                    todayRightArrow.visibility = View.GONE
                    todayLeftArrow.visibility = View.VISIBLE
                }
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayBtn, "translationY",  todayBtn.translationY, 0f))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener {
                    MainActivity.instance?.selectDate(System.currentTimeMillis())
                }
            }
            else -> {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(todayBtn, "translationY", todayBtn.translationY, tabSize.toFloat()))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
                todayBtn.setOnClickListener(null)
                /*
                todayBtn.setOnClickListener { _ ->
                    MainActivity.getDayPagerView()?.let {
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

    fun onDrag(event: DragEvent) {
        when(event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                deliveryDragEvent(event)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                deliveryDragEvent(event)
            }
            DragEvent.ACTION_DROP -> {
                deliveryDragEvent(event)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                calendarPagerView.endDrag()
                MainDragAndDropListener.end()
            }
        }
    }

    private fun deliveryDragEvent(event: DragEvent) {
        if(event.y > calendarPagerView.top && event.y < calendarPagerView.bottom) {
            calendarPagerView.onDrag(event)
        }else {

        }
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
            dayPagerView.isOpened() -> dayPagerView.hide()
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
                                resource.compress(Bitmap.CompressFormat.JPEG, 25, baos)
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
            if(dayPagerView.isOpened()) dayPagerView.hide()
            profileView.hide()
            showDialog(CalendarSettingsDialog(this@MainActivity),
                    true, false, true, false)
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        playIntentAction()
    }

    override fun onStop() {
        super.onStop()
        isShowing = false
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}