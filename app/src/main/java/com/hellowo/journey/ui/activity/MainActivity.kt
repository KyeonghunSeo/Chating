package com.hellowo.journey.ui.activity

import android.Manifest
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.hellowo.journey.*
import com.hellowo.journey.calendar.OsCalendarManager
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.ui.dialog.DatePickerDialog
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.DayView
import com.hellowo.journey.ui.view.SwipeScrollView.Companion.SWIPE_LEFT
import com.hellowo.journey.ui.view.SwipeScrollView.Companion.SWIPE_RIGHT
import com.hellowo.journey.viewmodel.MainViewModel
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
    }

    lateinit var viewModel: MainViewModel
    lateinit var dayView: DayView
    private val insertBtnHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TimeObjectManager.init()
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
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

    private fun initLayout() {
        window.navigationBarColor = resources.getColor(R.color.transitionDimWhite)
        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        dateLy.setOnClickListener {
            showDialog(DatePickerDialog(this@MainActivity, calendarView.selectedCal.timeInMillis) {
                calendarView.moveDate(it, true)
            }, true, true, true, false)
        }
        dimView.setOnDragListener(MainDragAndDropListener)
        callAfterViewDrawed(rootLy, Runnable{
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppRes.statusBarHeight = location[1]
        })
    }

    private fun initCalendarView() {
        calendarView.onDrawed = { cal ->
            setDateText(cal.time)
            briefingView.refreshTodayView(calendarView.todayStatus)
        }
        calendarView.onSelected = { time, cellNum, showDayView ->
            if(showDayView && dayView.viewMode == ViewMode.CLOSED) {
                dayView.show()
            }
            briefingView.refreshTodayView(calendarView.todayStatus)
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
        topShadow.visibility = View.VISIBLE
        calendarView.setOnTop { isTop ->
            //if(dayView.isOpened() || !isTop) topShadow.visibility = View.VISIBLE
            //else topShadow.visibility = View.GONE
        }
    }

    fun getCalendarView(): CalendarView = calendarView

    private fun initDayView() {
        dayView = DayView(calendarView, this@MainActivity)
        dayView.visibility = View.GONE
        calendarLy.addView(dayView, calendarLy.indexOfChild(topShadow))
        dayView.onVisibility = { show ->
            //if(show || !calendarView.isTop()) topShadow.visibility = View.VISIBLE
            //else topShadow.visibility = View.GONE
        }
    }

    private fun initDetailView() {
        timeObjectDetailView.initMap()
    }

    private fun initKeepView() {
        keepView.setOnClickListener {
            if(keepView.viewMode == ViewMode.CLOSED) { keepView.show() }
        }
    }

    private fun initBriefingView() {
        briefingView.setOnClickListener {
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
        menuBtn.setOnClickListener {
            //checkExternalStoragePermission()

            //startActivity(Intent(this, DrawActivity::class.java))

            viewModel.isCalendarSettingOpened.value = viewModel.isCalendarSettingOpened.value?.not() ?: true

            //checkOsCalendarPermission()
        }
    }

    private fun initObserver() {
        viewModel.targetTimeObject.observe(this, Observer { timeObject ->
            if(timeObject != null) {
                timeObjectDetailView.show(timeObject)
            }else {
                timeObjectDetailView.hide()
            }
        })

        viewModel.appUser.observe(this, Observer { it?.let { updateUserUI(it) } })

        viewModel.templateList.observe(this, Observer {
            it?.let { templateControlView.notify(it) }
        })

        viewModel.isCalendarSettingOpened.observe(this, Observer { isOpend ->
            isOpend?.let {
                TransitionManager.beginDelayedTransition(calendarLy, makeChangeBounceTransition())
                (calendarLy.layoutParams as FrameLayout.LayoutParams).let {
                    if(isOpend) it.topMargin = dpToPx(150)
                    else it.topMargin = dpToPx(0)
                }
                calendarLy.requestLayout()
            }
        })
    }

    private fun updateUserUI(appUser: AppUser) {
        if(appUser.profileImg?.isNotEmpty() == true) {
            profileImage.colorFilter = null
            Glide.with(this).load(appUser.profileImg)
                    .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }else {
            profileImage.setColorFilter(resources.getColor(R.color.iconTint))
        }
    }

    private fun setDateText(date: Date) {
        monthText.text = AppRes.ymDate.format(date)
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

    fun onDimDark(animation: Boolean, eventBackPressed: Boolean) {
        dimView.setOnClickListener { if(eventBackPressed) onBackPressed() }
        dimView.isClickable = true
        if (animation) {
            ObjectAnimator.ofFloat(dimView, "alpha", 0f, 1f).setDuration(200).start()
        } else {
            dimView.alpha = 1f
        }
        statusBarBlackAlpah(this)
    }

    fun offDimDark(animation: Boolean, dark: Boolean) {
        dimView.setOnClickListener(null)
        dimView.isClickable = false
        if (animation) {
            ObjectAnimator.ofFloat(dimView, "alpha", 1f, 0f).setDuration(200).start()
        } else {
            dimView.alpha = 0f
        }
        statusBarWhite(this)
    }

    private fun checkExternalStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_PERMISSIONS)
        } else {
            showPhotoPicker()
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
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showPhotoPicker() }
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.READ_CALENDAR && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> OsCalendarManager.getCalendarList(this) }
                return
            }
        }
    }

    private fun showPhotoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), RC_FILEPICKER)
        } catch (ex: android.content.ActivityNotFoundException) { ex.printStackTrace() }
    }

    override fun onBackPressed() {
        when{
            timeObjectDetailView.isOpened() -> timeObjectDetailView.hide()
            templateControlView.isExpanded -> templateControlView.collapse()
            keepView.viewMode == ViewMode.OPENED -> keepView.hide()
            briefingView.viewMode == ViewMode.OPENED -> briefingView.hide()
            dayView.isOpened() -> dayView.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        timeObjectDetailView.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_FILEPICKER && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                try{
                    Glide.with(this).asBitmap().load(uri)
                            .into(object : SimpleTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    l("사진 크기 : ${resource.rowBytes} 바이트")
                                    viewModel.saveProfileImage(resource)
                                }
                            })
                }catch (e: Exception){}
            }
        }else if(requestCode == RC_OS_CALENDAR) {
            dayView.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        isShowing = true
        playIntentAction()
        super.onResume()
    }

    override fun onStop() {
        isShowing = false
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}