package com.hellowo.journey.ui.activity

import android.Manifest
import android.animation.LayoutTransition
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.hellowo.journey.*
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.ui.dialog.DatePickerDialog
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.DayView
import com.hellowo.journey.ui.view.base.SwipeScrollView.Companion.SWIPE_LEFT
import com.hellowo.journey.ui.view.base.SwipeScrollView.Companion.SWIPE_RIGHT
import com.hellowo.journey.viewmodel.MainViewModel
import androidx.lifecycle.Observer
import com.hellowo.journey.manager.FolderManager
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.view.WindowManager
import android.os.Build
import io.realm.Realm
import io.realm.RealmAsyncTask





class MainActivity : AppCompatActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
    }

    lateinit var viewModel: MainViewModel
    lateinit var dayView: DayView
    private var realm: Realm? = null
    private var realmAsyncTask: RealmAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        if(SyncUser.current() == null) {
            startActivityForResult(Intent(this, WelcomeActivity::class.java), RC_LOGIN)
        }else {
            initMain()
        }
    }

    private fun initMain() {
        initRealm()
        initTheme()
        initLayout()
        initCalendarView()
        initDayView()
        initDetailView()
        initKeepView()
        initBriefingView()
        initTemplateView()
        initBtns()
        initObserver()
    }

    private fun initRealm() {
        viewModel.loading.value = true
        val config = SyncUser.current()
                .createConfiguration(USER_URL)
                .fullSynchronization()
                .waitForInitialRemoteData()
                .build()
        Realm.setDefaultConfiguration(config)
        realmAsyncTask = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                if (isDestroyed) {
                    realm.close()
                } else {
                    l("Realm 데이터베이스 준비 완료")
                    this@MainActivity.realm = realm
                    viewModel.loading.value = false
                    viewModel.init()
                }
            }
        })
    }

    private fun playIntentAction() {
        playAction(intent.getIntExtra("action", 0), intent.getBundleExtra("bundle"))
        intent.removeExtra("action")
    }

    fun playAction(action: Int, bundle: Bundle?) {
        when(action) {
            1 -> briefingView.show()
            2 -> {
                bundle?.let {
                    viewModel.setTargetTimeObjectById(bundle.getString("timeObjectId"))
                }
            }
        }
    }

    private fun initTheme() {
        window.navigationBarColor = AppTheme.backgroundColor
        window.statusBarColor = AppTheme.backgroundColor
        calendarLy.setBackgroundColor(AppTheme.backgroundColor)
    }

    private fun initLayout() {
        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        dateLy.setOnClickListener { _ ->
            showDialog(DatePickerDialog(this, calendarView.targetCal.timeInMillis) {
                calendarView.moveDate(it, true)
            }, true, true, true, false)
        }
        dimView.setOnDragListener(MainDragAndDropListener)
        callAfterViewDrawed(rootLy, Runnable{
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppDateFormat.statusBarHeight = location[1]
        })
    }

    private fun initCalendarView() {
        calendarView.onDrawed = { cal ->
            setDateText(cal.time)
        }
        calendarView.onSelected = { time, cellNum, showDayView ->
            if(cellNum >= 0) {
                if(showDayView && !dayView.isOpened()) {
                    dayView.show()
                }else if(dayView.isOpened()){
                    dayView.notifyDateChanged(0)
                }
                briefingView.refreshTodayView(calendarView.todayStatus)
                if(calendarView.todayStatus == 0) {
                    todayText.alpha = 1f
                    todayText.text = "Today\nBriefing"
                }else {
                    todayText.alpha = 0.3f
                    todayText.text = "Today"
                }
            }else {
                TransitionManager.beginDelayedTransition(templateSelectView, makeFromBottomSlideTransition())
                templateSelectView.visibility = View.INVISIBLE
            }
        }
        calendarView.setOnSwiped { state ->
            if(dayView.isOpened()) {
                when(state) {
                    SWIPE_LEFT -> {
                        vibrate(this)
                        calendarView.moveDate(-1, true)
                        dayView.notifyDateChanged(-1)
                    }
                    SWIPE_RIGHT -> {
                        vibrate(this)
                        calendarView.moveDate(1, true)
                        dayView.notifyDateChanged(1)
                    }
                }
            }else {
                when(state) {
                    SWIPE_LEFT -> {
                        vibrate(this)
                        calendarView.moveMonth(-1)
                    }
                    SWIPE_RIGHT -> {
                        vibrate(this)
                        calendarView.moveMonth(1)
                    }
                }
            }
        }
        calendarView.setOnTop { isTop ->
            //if(dayView.isOpened() || !isTop) topBar.elevation = dpToPx(2f)
            //else topBar.elevation = dpToPx(0f)
        }
    }

    fun getCalendarView(): CalendarView = calendarView

    private fun initDayView() {
        dayView = DayView(calendarView, this)
        dayView.visibility = View.GONE
        calendarLy.addView(dayView, calendarLy.indexOfChild(dimView))
        dayView.onVisibility = { show ->
            //if(show || !calendarView.isTop()) topBar.elevation = dpToPx(0f)
            //else topBar.elevation = dpToPx(2f)
        }
    }

    private fun initDetailView() {
        timeObjectDetailView.initMap()
    }

    private fun initKeepView() {
        keepBtn.setOnClickListener { viewModel.targetFolder.value = FolderManager.getPrimaryFolder() }
    }

    private fun initBriefingView() {
        todayBtn.setOnClickListener {
            if(calendarView.todayStatus != 0) {
                calendarView.moveDate(System.currentTimeMillis(), true)
            }else {
                briefingView.show()
            }
        }
    }

    private fun initTemplateView() {

    }

    private fun initBtns() {
        profileImage.setOnClickListener {
            //checkExternalStoragePermission(RC_PRFOFILE_IMAGE)

            //startActivity(Intent(this, DrawActivity::class.java))

            //viewModel.isCalendarSettingOpened.value = viewModel.isCalendarSettingOpened.value?.not() ?: true

            checkOsCalendarPermission()
        }

        profileImage.setOnLongClickListener {
            startActivity(Intent(this, TemplateEditActivity::class.java))
            return@setOnLongClickListener true
        }
    }

    private fun initObserver() {
        viewModel.loading.observe(this, Observer {
            if(it as Boolean) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        })

        viewModel.targetTimeObject.observe(this, Observer { timeObject ->
            if(timeObject != null) {
                timeObjectDetailView.show(timeObject)
            }else {
                timeObjectDetailView.hide()
            }
        })

        viewModel.appUser.observe(this, Observer { appUser -> appUser?.let { updateUserUI(it) } })

        viewModel.templateList.observe(this, Observer { list ->
            list?.let { templateSelectView.notify(it) }
        })

        viewModel.isCalendarSettingOpened.observe(this, Observer { isOpend ->
            isOpend?.let { _ ->
                TransitionManager.beginDelayedTransition(calendarLy, makeChangeBounceTransition())
                (calendarLy.layoutParams as FrameLayout.LayoutParams).let {
                    if(isOpend) it.topMargin = dpToPx(150)
                    else it.topMargin = dpToPx(0)
                }
                calendarLy.requestLayout()
            }
        })

        viewModel.targetFolder.observe(this, Observer { folder ->
            if (folder != null) {
                if (keepView.viewMode == ViewMode.CLOSED) {
                    keepView.show()
                }
            } else {
                keepView.hide()
            }
        })
    }

    private fun updateUserUI(appUser: AppUser) {
        if(appUser.profileImg?.isNotEmpty() == true) {
            profileImage.clearColorFilter()
            Glide.with(this).load(appUser.profileImg)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }else {
            profileImage.setColorFilter(AppTheme.primaryText)
        }
    }

    private fun setDateText(date: Date) {
        yearText.typeface = AppTheme.textFont
        monthText.typeface = AppTheme.textFont
        yearText.text = AppDateFormat.year.format(date)
        monthText.text = AppDateFormat.monthEng.format(date)
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
                calendarView.endDrag()
                MainDragAndDropListener.end()
            }
        }
    }

    fun deliveryDragEvent(event: DragEvent) {
        if(event.y > calendarView.top && event.y < calendarView.bottom) {
            calendarView.onDrag(event)
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

    private fun checkOsCalendarPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
        } else {
            OsCalendarManager.getCalendarList(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.READ_CALENDAR && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> OsCalendarManager.getCalendarList(this) }
                return
            }
            RC_IMAGE_ATTACHMENT, RC_PRFOFILE_IMAGE -> {
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

    override fun onBackPressed() {
        when{
            searchView.isOpened() -> searchView.hide()
            timeObjectDetailView.isOpened() -> viewModel.targetTimeObject.value = null
            templateSelectView.isExpanded -> templateSelectView.collapse()
            keepView.viewMode == ViewMode.OPENED -> viewModel.targetFolder.value = null
            briefingView.viewMode == ViewMode.OPENED -> briefingView.hide()
            dayView.isOpened() -> dayView.hide()
            //calendarView.selectCellNum >= 0 -> calendarView.unselectDate(calendarView.selectCellNum, true)
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        timeObjectDetailView.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_LOGIN && resultCode == RESULT_OK) {
            initMain()
        }else if (requestCode == RC_PRFOFILE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                Glide.with(this).asBitmap().load(uri)
                        .into(object : SimpleTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                l("사진 크기 : ${resource.rowBytes} 바이트")
                                viewModel.saveProfileImage(resource)
                            }
                        })
            }
        }else if(requestCode == RC_OS_CALENDAR) {
            dayView.onActivityResult(requestCode, resultCode, data)
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
        TimeObjectManager.clear()
        realm?.removeAllChangeListeners()
        realm?.close()
        realm = null
        realmAsyncTask?.cancel()
    }
}