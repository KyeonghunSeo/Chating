package com.hellowo.journey.ui.activity

import android.Manifest
import android.animation.LayoutTransition
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hellowo.journey.*
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.dialog.DatePickerDialog
import com.hellowo.journey.viewmodel.MainViewModel
import com.theartofdev.edmodo.cropper.CropImage
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : BaseActivity() {
    companion object {
        var instance: MainActivity? = null
        var isShowing = false
        fun getViewModel() = instance?.viewModel
        fun getDayPagerView() = instance?.dayPagerView
        fun getCalendarPagerView() = instance?.calendarPagerView
        fun getTargetCalendarView() = instance?.viewModel?.targetCalendarView?.value
        fun getTargetTime() = instance?.viewModel?.targetTime?.value
        fun getTargetCal() = instance?.viewModel?.targetCalendarView?.value?.targetCal
    }

    lateinit var viewModel: MainViewModel
    private var reservedIntentAction: Runnable? = null
    private var keypadListener : Unregistrar? = null

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
        topMenu.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        monthText.setOnClickListener { _ ->
            showDialog(DatePickerDialog(this, viewModel.targetTime.value!!) {
                selectDate(it)
            }, true, true, true, false)
        }
        rootLy.setOnDragListener(MainDragAndDropListener)

        keypadListener = KeyboardVisibilityEvent.registerEventListener(MainActivity.instance) { isOpen ->
            l("키보드 상태 $isOpen")
            timeObjectDetailView.setKeyboardLy(isOpen)
            if(isOpen) {

            }else {

            }
        }

        callAfterViewDrawed(rootLy, Runnable{
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppDateFormat.statusBarHeight = location[1]
        })
    }

    fun selectDate(time: Long) {
        calendarPagerView.selectDate(time)
        if(dayPagerView.isOpened()){
            dayPagerView.initTime(time)
            dayPagerView.notifyDateChanged()
        }
    }

    private fun initCalendarView() {
        calendarPagerView.onSelectedDate = { time, cellNum, isSameSeleted, calendarView ->
            viewModel.targetTime.value = time
            viewModel.targetCalendarView.value = calendarView
            if(cellNum >= 0) {
                if(isSameSeleted && dayPagerView.viewMode == ViewMode.CLOSED) dayPagerView.show()
                briefingView.refreshTodayView(calendarView.todayStatus)
            }else {
                //TransitionManager.beginDelayedTransition(templateControlView, makeFromBottomSlideTransition())
                //templateControlView.visibility = View.INVISIBLE
            }
        }
    }

    private fun initDayView() {
        dayPagerView.onVisibility = { show -> }
    }

    private fun initDetailView() {
        timeObjectDetailView.initMap()
    }

    private fun initKeepView() {}

    private fun initBriefingView() {}

    private fun initTemplateView() {}

    private fun initBtns() {
        keepBtn.setOnClickListener {
            if(viewModel.currentTab.value != 1) viewModel.currentTab.value = 1
        }

        keepBtn.setOnLongClickListener {
            //TimeObjectManager.deleteAllTimeObject()
            AppTheme.thinFont = AppTheme.tFont
            AppTheme.regularFont = AppTheme.rFont
            AppTheme.boldFont = AppTheme.bFont
            setGlobalTheme(rootLy)
            return@setOnLongClickListener false
        }

        profileBtn.setOnClickListener {
            profileView.show()
        }

        profileBtn.setOnLongClickListener {
            //viewModel.isCalendarSettingOpened.value = viewModel.isCalendarSettingOpened.value?.not() ?: true

            val cal = Calendar.getInstance()
            cal.set(2019, 3, 1)
            val s = cal.timeInMillis
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s, s).apply {
                title = "점심약속"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s, s).apply {
                title = "오후미팅"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s+DAY_MILL, s+DAY_MILL).apply {
                title = "헬스장"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s, s+DAY_MILL*3).apply {
                title = "회사 프로젝트"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s+DAY_MILL*28, s+DAY_MILL*28).apply {
                title = "점심약속"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s+DAY_MILL*28, s+DAY_MILL*29).apply {
                title = "오후미팅"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s+DAY_MILL*28, s+DAY_MILL*30).apply {
                title = "헬스장"
                type = 1
            })
            TimeObjectManager.save(TimeObjectManager.makeNewTimeObject(s+DAY_MILL*29, s+DAY_MILL*29).apply {
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
            if(list.size > 1) keepBtn.setImageResource(R.drawable.outline_all_inbox_black_48dp)
            else keepBtn.setImageResource(R.drawable.outline_inbox_black_48dp)
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

        viewModel.targetCalendarView.observe(this, Observer { _ -> setDateText() })

        viewModel.currentTab.observe(this, Observer { index -> updateUI(index)})
    }

    private fun updateUI(index: Int) {
        when(index) {
            0 -> {
                if(keepView.isOpened()) viewModel.targetFolder.value = null
                if(searchView.isOpened()) searchView.hide()
                if(profileView.isOpened()) profileView.hide()
            }
            1 -> {
                viewModel.setTargetFolder()
                if(searchView.isOpened()) searchView.hide()
                if(profileView.isOpened()) profileView.hide()
            }
            2 -> {
                if(keepView.isOpened()) viewModel.targetFolder.value = null
                searchView.show()
                if(profileView.isOpened()) profileView.hide()
                startDialogShowAnimation(searchView)
            }
        }
    }

    private fun updateUserUI(appUser: AppUser) {
        profileView.updateUserUI(appUser)
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        getTargetCal()?.let {
            monthText.text = AppDateFormat.ymDate.format(it.time)
            // + " " + String.format(getString(R.string.weekNum), it.get(Calendar.WEEK_OF_YEAR))
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
            profileView.isOpened() -> profileView.hide()
            viewModel.currentTab.value != 0 -> viewModel.currentTab.value = 0
            dayPagerView.isOpened() -> dayPagerView.hide()
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
                                val ref = FirebaseStorage.getInstance().reference.child("${viewModel.appUser.value?.id}/profileImg.jpg")
                                val baos = ByteArrayOutputStream()
                                resource.compress(Bitmap.CompressFormat.JPEG, 25, baos)
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
            refreshAll()
        }
    }

    private fun refreshAll() {
        calendarPagerView.redraw()
        if(dayPagerView.isOpened()){
            dayPagerView.notifyDateChanged()
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
        keypadListener?.unregister()
    }
}