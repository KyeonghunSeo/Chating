package com.hellowo.chating.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.DateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.hellowo.chating.*
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.model.AppUser
import com.hellowo.chating.ui.view.SwipeScrollView.Companion.SWIPE_LEFT
import com.hellowo.chating.ui.view.SwipeScrollView.Companion.SWIPE_RIGHT
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var instance: MainActivity? = null
    }

    lateinit var viewModel: MainViewModel
    private val insertBtnHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
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
        initKeepView()
        initBriefingView()
        initBtns()
        initObserver()
    }

    private fun initLayout() {
        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        timeObjectDetailView.initBehavior()
    }

    private fun initCalendarView() {
        calendarView.onDrawed = { cal -> setDateText(cal.time) }
        calendarView.onSelected = { time, cellNum, showDayView ->
            if(showDayView && dayView.viewMode == ViewMode.CLOSED) {
                dayView.show()
            }
        }
        calendarView.setOnSwiped { state ->
            if(dayView.viewMode == ViewMode.OPENED) {
                when(state) {
                    SWIPE_LEFT -> {
                        vibrate(this)
                        calendarView.moveDate(-1)
                        dayView.notifyDateChanged(-1)
                    }
                    SWIPE_RIGHT -> {
                        vibrate(this)
                        calendarView.moveDate(1)
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
            //if(!isTop) topBar.elevation = dpToPx(2).toFloat()
            //else topBar.elevation = 0f
        }

    }

    private fun initDayView() {
        dayView.setCalendarView(calendarView)
    }

    private fun initKeepView() {
        keepView.mainActivity = this
        keepView.setOnClickListener {
            if(keepView.viewMode == ViewMode.CLOSED) { keepView.show() }
        }
    }

    private fun initBriefingView() {
        briefingView.setOnClickListener {
            if(briefingView.viewMode == ViewMode.CLOSED) { briefingView.show() }
        }
    }

    private fun initBtns() {
        insertBtn.setOnClickListener {
            if(timeObjectDetailView.viewMode == ViewMode.CLOSED) {
                viewModel.targetTimeObject.value = TimeObjectManager.makeNewTimeObject(
                        getCalendarTime0(calendarView.selectedCal), getCalendarTime23(calendarView.selectedCal))
            }else {

            }
        }

        insertBtn.setOnLongClickListener {
            return@setOnLongClickListener false
        }

        menuBtn.setOnClickListener {
            checkExternalStoragePermission()
        }
    }

    private fun initObserver() {
        viewModel.targetTimeObject.observe(this, androidx.lifecycle.Observer { timeObject ->
            if(timeObject != null) {
                timeObjectDetailView.show(timeObject)
            }else {
                timeObjectDetailView.hide()
            }
        })
        viewModel.appUser.observe(this, androidx.lifecycle.Observer { it?.let { updateUserUI(it) } })
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
        monthText.text = simpleYMDf.format(date)
    }

    fun onDimDark(animation: Boolean, dark: Boolean) {
        dimView.setOnClickListener { onBackPressed() }
        dimView.isClickable = true
        if (animation) {
            ObjectAnimator.ofFloat(dimView, "alpha", 0f, 1f).setDuration(50).start()
        } else {
            dimView.alpha = 1f
        }
        statusBarBlackAlpah(this)
    }

    fun offDimDark(animation: Boolean, dark: Boolean) {
        dimView.setOnClickListener(null)
        dimView.isClickable = false
        if (animation) {
            ObjectAnimator.ofFloat(dimView, "alpha", 1f, 0f).setDuration(50).start()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { showPhotoPicker() }
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
            timeObjectDetailView.viewMode == ViewMode.OPENED -> timeObjectDetailView.hide()
            keepView.viewMode == ViewMode.OPENED -> keepView.hide()
            briefingView.viewMode == ViewMode.OPENED -> briefingView.hide()
            dayView.viewMode == ViewMode.OPENED -> dayView.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_FILEPICKER && resultCode == Activity.RESULT_OK) {
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}