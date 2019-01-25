package com.hellowo.journey.ui.activity

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import io.realm.Realm
import io.realm.RealmAsyncTask
import java.io.ByteArrayOutputStream

class MainActivity : BaseActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
    }

    lateinit var viewModel: MainViewModel
    lateinit var dayView: DayView
    private var reservedIntentAction: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        initMain()
        initTheme(rootLy)
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
        initDayView()
        initDetailView()
        initKeepView()
        initBriefingView()
        initTemplateView()
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
            1 -> briefingView.show()
            2 -> {
                bundle?.let {
                    viewModel.setTargetTimeObjectById(bundle.getString("timeObjectId"))
                }
            }
        }
    }

    private fun initLayout() {
        yearText.typeface = AppTheme.boldFont
        monthText.typeface = AppTheme.boldFont
        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //todayBtn.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        dateLy.setOnClickListener { _ ->
            showDialog(DatePickerDialog(this, calendarView.targetCal.timeInMillis) {
                calendarView.moveDate(it, true)
            }, true, true, true, false)
        }
        calendarLy.setOnDragListener(MainDragAndDropListener)
        searchBtn.setOnClickListener { searchView.show() }

        callAfterViewDrawed(rootLy, Runnable{
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppDateFormat.statusBarHeight = location[1]
        })
    }

    private fun initCalendarView() {
        calendarView.onSelected = { time, cellNum, showDayView ->
            viewModel.targetTime.value = time
            if(cellNum >= 0) {
                if(showDayView && !dayView.isOpened()) {
                    dayView.show()
                }else if(dayView.isOpened()){
                    dayView.notifyDateChanged(0)
                }
                briefingView.refreshTodayView(calendarView.todayStatus)
            }else {
                TransitionManager.beginDelayedTransition(templateControlView, makeFromBottomSlideTransition())
                templateControlView.visibility = View.INVISIBLE
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
        dayView.onVisibility = { show ->
            //if(show || !calendarView.isTop()) topBar.elevation = dpToPx(0f)
            //else topBar.elevation = dpToPx(2f)
            if(show) calendarBtn.setImageResource(R.drawable.sharp_calendar_black_48dp)
            else calendarBtn.setImageResource(R.drawable.sharp_event_black_48dp)
        }
        calendarLy.addView(dayView, calendarLy.indexOfChild(calendarView) + 1)
    }

    private fun initDetailView() {
        timeObjectDetailView.initMap()
    }

    private fun initKeepView() {

    }

    private fun initBriefingView() {
    }

    private fun initTemplateView() {

    }

    private fun initBtns() {
        calendarBtn.setOnClickListener {
            if(viewModel.currentTab.value == 0) {
                if(dayView.isOpened()) dayView.hide()
                else if(dayView.viewMode == ViewMode.CLOSED) dayView.show()
            }
            else viewModel.currentTab.value = 0
        }

        keepBtn.setOnClickListener {
            if(viewModel.currentTab.value != 1) viewModel.currentTab.value = 1
        }

        searchBtn.setOnClickListener {
            if(viewModel.currentTab.value != 2) viewModel.currentTab.value = 2
        }

        profileBtn.setOnClickListener {
            if(viewModel.currentTab.value != 3) viewModel.currentTab.value = 3
        }

        profileBtn.setOnLongClickListener {
            profileView.checkOsCalendarPermission()
            //viewModel.isCalendarSettingOpened.value = viewModel.isCalendarSettingOpened.value?.not() ?: true
            return@setOnLongClickListener true
        }

        prevBtn.setOnClickListener {
            if(dayView.isOpened()) {
                calendarView.targetCal.add(Calendar.DATE, -1)
            }else {
                calendarView.targetCal.set(Calendar.DATE, 1)
                calendarView.targetCal.add(Calendar.MONTH, -1)
            }
            calendarView.moveDate(calendarView.targetCal.timeInMillis, true)
        }

        nextBtn.setOnClickListener {
            if(dayView.isOpened()) {
                calendarView.targetCal.add(Calendar.DATE, 1)
            }else {
                calendarView.targetCal.set(Calendar.DATE, 1)
                calendarView.targetCal.add(Calendar.MONTH, 1)
            }
            calendarView.moveDate(calendarView.targetCal.timeInMillis, true)
        }
    }

    private fun initObserver() {
        viewModel.realm.observe(this, Observer { realm ->
            if(realm != null) {
                calendarView.moveDate(System.currentTimeMillis(), true)
                reservedIntentAction?.run()
                reservedIntentAction = null
            }
        })

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
            list?.let { templateControlView.notify(it) }
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

        viewModel.folderList.observe(this, Observer { list ->
            if(list.size > 1) keepBtn.setImageResource(R.drawable.sharp_all_inbox_black_48dp)
            else keepBtn.setImageResource(R.drawable.sharp_inbox_black_48dp)
            keepView.notifyFolderDataChanged()
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

        viewModel.targetTime.observe(this, Observer { time -> setDateText(Date(time)) })

        viewModel.currentTab.observe(this, Observer { index -> updateUI(index)})
    }

    private fun updateUI(index: Int) {
        calendarBtn.setColorFilter(AppTheme.disableText)
        keepBtn.setColorFilter(AppTheme.disableText)
        searchBtn.setColorFilter(AppTheme.disableText)
        profileBtn.setColorFilter(AppTheme.disableText)

        when(index) {
            0 -> {
                calendarBtn.setColorFilter(AppTheme.primaryColor)
                if(keepView.isOpened()) viewModel.targetFolder.value = null
                if(searchView.isOpened()) searchView.hide()
                if(profileView.isOpened()) profileView.hide()
            }
            1 -> {
                keepBtn.setColorFilter(AppTheme.primaryColor)
                viewModel.setTargetFolder()
                if(searchView.isOpened()) searchView.hide()
                if(profileView.isOpened()) profileView.hide()
            }
            2 -> {
                searchBtn.setColorFilter(AppTheme.primaryColor)
                if(keepView.isOpened()) viewModel.targetFolder.value = null
                searchView.show()
                if(profileView.isOpened()) profileView.hide()
            }
            3 -> {
                profileBtn.setColorFilter(AppTheme.primaryColor)
                if(keepView.isOpened()) viewModel.targetFolder.value = null
                if(searchView.isOpened()) searchView.hide()
                profileView.show()
            }
        }
    }

    private fun updateUserUI(appUser: AppUser) {
        profileView.updateUserUI(appUser)
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText(date: Date) {
        //yearText.text = " ${AppDateFormat.year.format(date)}"
        //monthText.text = String.format("%02d", date.month + 1)
        yearText.text = ""
        monthText.text = AppDateFormat.ymDate.format(date)
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

    fun expandControlView(dtStart: Long, dtEnd: Long) {
        templateControlView.expand(dtStart, dtEnd)
    }

    override fun onBackPressed() {
        when{
            timeObjectDetailView.isOpened() -> timeObjectDetailView.confirm()
            templateControlView.isExpanded -> templateControlView.collapse()
            viewModel.currentTab.value != 0 -> viewModel.currentTab.value = 0
            dayView.isOpened() -> dayView.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        timeObjectDetailView.onActivityResult(requestCode, resultCode, data)
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
                                val ref = FirebaseStorage.getInstance().reference.child("${viewModel.appUser.value?.id}/profileImg.jpg")
                                val baos = ByteArrayOutputStream()
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val uploadTask = ref.putBytes(baos.toByteArray())
                                uploadTask.addOnFailureListener {
                                    hideProgressDialog()
                                }.addOnSuccessListener { _ ->
                                    ref.downloadUrl.addOnCompleteListener {
                                        l("다운로드 url : ${it.result.toString()}")
                                        viewModel.saveProfileImage(it.result.toString())
                                        hideProgressDialog()
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
    }
}