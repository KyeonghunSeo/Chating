package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.model.*
import com.ayaan.twelvepages.ui.dialog.OsCalendarDialog
import com.ayaan.twelvepages.ui.view.RecordView
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import io.realm.*
import kotlinx.android.synthetic.main.activity_welcome.*
import java.io.File
import java.util.*


class WelcomeActivity : BaseActivity() {
    private var realmAsyncTask: RealmAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initTheme(rootLy)
        callAfterViewDrawed(rootLy, Runnable{
            val rectangle = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight = rectangle.top
            val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
            val titleBarHeight = contentViewTop - statusBarHeight
            val location = IntArray(2)
            rootLy.getLocationInWindow(location)
            AppStatus.statusBarHeight = location[1]
            //l("StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight + " , AppStatus.statusBarHeight = " + AppStatus.statusBarHeight)

            if(FirebaseAuth.getInstance().currentUser == null) {
                startShow()
            }else {
                startMainActivity()
                //startCustomSettings()
                //startShow()
            }
        })
    }

    private fun startShow() {
        loginBtn.setOnClickListener { signInWithGoogle() }
        val handler =  Handler()
        handler.postDelayed({
            leafFallView.start()
            ObjectAnimator.ofFloat(firstText1, "alpha", 0f, 1f).let {
                it.duration = 1000
                it.start()
            }
        }, 1000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText2, "alpha", 0f, 1f).let {
                it.duration = 1000
                it.start()
            }
        }, 3500)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText3, "alpha", 0f, 1f).let {
                it.duration = 1000
                it.start()
            }
        }, 6000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText1, "alpha", 1f, 0f).let {
                it.duration = 1000
                it.start()
            }
        }, 8500)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText2, "alpha", 1f, 0f).let {
                it.duration = 1000
                it.start()
            }
        }, 10000)
        handler.postDelayed({
            ObjectAnimator.ofFloat(firstText3, "alpha", 1f, 0f).let {
                it.duration = 1000
                it.start()
            }
        }, 11500)
        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(loginLy, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(loginLy, "translationY", dpToPx(15f), 0f))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 13000)
    }

    private fun startCustomSettings() {
        leafFallView.stop()
        val handler =  Handler()
        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(loginLy, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(loginLy, "translationY", 0f, dpToPx(15f)))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 0)

        handler.postDelayed({
            loginLy.visibility = View.GONE
            initSettingLy.visibility = View.VISIBLE
            setDiaryLy()
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(initSettingLy, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(initSettingLy, "translationY", dpToPx(15f), 0f))
            animSet.duration = 1000
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }, 1000)
    }

    var doubleTabFlag = true
    var diaryNum = 0

    private fun setDiaryLy() {
        val user = FirebaseAuth.getInstance().currentUser
        optionTitleText.text = String.format(getString(R.string.init_setting_script_0), user?.displayName)
        diaryLy.visibility = View.VISIBLE
        prevBtn.visibility = View.GONE
        nextBtn.setOnClickListener {
            if(doubleTabFlag) {
                doubleTabFlag = false
                hideScript()

                val realm = Realm.getDefaultInstance()
                realm.executeTransaction {
                    try{
                        realm.createObject(Folder::class.java, "calendar").apply {
                            name = App.context.getString(R.string.calendar)
                            type = 0
                            order = 0
                        }
                    }catch (e: Exception){e.printStackTrace()}

                    realm.where(Folder::class.java).equalTo("id", "calendar").findFirst()?.let { f ->
                        realm.where(Template::class.java).findAll()?.deleteAllFromRealm() /* 이부분 지워도 됨 */

                        val record = Record()
                        var od = realm.where(Template::class.java).max("order")?.toInt() ?: -1
                        when(diaryNum) {
                            0 -> {
                                var importantTag: Tag? = null
                                val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                                importantTag = realm.where(Tag::class.java).equalTo("title", str(R.string.important)).findFirst()
                                if(importantTag == null) {
                                    importantTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                                    importantTag?.title = str(R.string.important)
                                    importantTag?.order = tagOrder + 1
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RECT_FILL_BLUR)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.event)
                                    style = record.style
                                    colorKey = 0
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.UNDER_LINE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.todo)
                                    style = record.style
                                    colorKey = 0
                                    setCheckBox()
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                                record.setShape(RecordView.Shape.COLOR_PEN)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.memo)
                                    style = record.style
                                    clearTitle()
                                    setMemo()
                                    colorKey = 0
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RECT_FILL_BLUR)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.important_event)
                                    style = record.style
                                    colorKey = 4
                                    alarmDayOffset = 0
                                    alarmTime = AlarmManager.defaultAlarmTime[0]
                                    importantTag?.let { tags.add(it) }
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RANGE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.plan)
                                    style = record.style
                                    colorKey = 0
                                }
                            }
                            1 -> {
                                var importantTag: Tag? = null
                                var emerTag: Tag? = null
                                val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                                importantTag = realm.where(Tag::class.java).equalTo("title", str(R.string.important)).findFirst()
                                if(importantTag == null) {
                                    importantTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                                    importantTag?.title = str(R.string.important)
                                    importantTag?.order = tagOrder + 1
                                }
                                emerTag = realm.where(Tag::class.java).equalTo("title", str(R.string.emergency)).findFirst()
                                if(emerTag == null) {
                                    emerTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                                    emerTag?.title = str(R.string.emergency)
                                    emerTag?.order = tagOrder + 2
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RECT_FILL)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.event)
                                    style = record.style
                                    colorKey = 2
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.BOLD_HATCHED)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.important_event)
                                    style = record.style
                                    alarmDayOffset = 0
                                    alarmTime = AlarmManager.defaultAlarmTime[0]
                                    colorKey = 4
                                    setTime()
                                    importantTag?.let { tags.add(it) }
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.DOT)
                                record.setShape(RecordView.Shape.BLANK)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.todo)
                                    style = record.style
                                    colorKey = 0
                                    setCheckBox()
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.UNDER_LINE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.important_todo)
                                    style = record.style
                                    colorKey = 0
                                    alarmDayOffset = 0
                                    alarmTime = AlarmManager.defaultAlarmTime[0]
                                    setCheckBox()
                                    emerTag?.let { tags.add(it) }
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RANGE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.plan)
                                    style = record.style
                                    colorKey = 6
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                                record.setShape(RecordView.Shape.COLOR_PEN)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.memo)
                                    style = record.style
                                    colorKey = 0
                                    clearTitle()
                                    setMemo()
                                }
                            }
                            2 -> {
                                var diaryTag: Tag? = null
                                val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                                diaryTag = realm.where(Tag::class.java).equalTo("title", str(R.string.diary)).findFirst()
                                if(diaryTag == null) {
                                    diaryTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                                    diaryTag?.title = str(R.string.diary)
                                    diaryTag?.order = tagOrder + 1
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                                record.setShape(RecordView.Shape.UPPER_LINE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.diary)
                                    style = record.style
                                    colorKey = 0
                                    setMemo()
                                    diaryTag?.let { tags.add(it) }
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.DOT)
                                record.setShape(RecordView.Shape.BLANK)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.memo)
                                    style = record.style
                                    colorKey = 0
                                    clearTitle()
                                    setMemo()
                                }

                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                                record.setShape(RecordView.Shape.RANGE)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.plan)
                                    style = record.style
                                    colorKey = 0
                                }
                            }
                            else -> {
                                od++
                                record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                                record.setShape(RecordView.Shape.COLOR_PEN)
                                realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                                    folder = f
                                    order = od
                                    title = str(R.string.new_record)
                                    style = record.style
                                    colorKey = 0
                                }

                            }
                        }
                    }
                }
                realm.close()

                initSettingLy.postDelayed({
                    setCalendarOptionLy()
                    showScript()
                }, 1000)
            }
        }

        val btns = arrayOf(diaryBtn0, diaryBtn1, diaryBtn2, diaryBtn3)
        val selectors = arrayOf(diarySelector0, diarySelector1, diarySelector2, diarySelector3)
        btns.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                diaryNum = index
                selectors.forEachIndexed { i, imageView ->
                    imageView.visibility = if(index == i) View.VISIBLE else View.GONE
                    ObjectAnimator.ofFloat(btns[i], "cardElevation", if(index == i) dpToPx(30f) else dpToPx(1f)).start()
                    ObjectAnimator.ofFloat(btns[i], "alpha", if(index == i) 1f else 0.5f).start()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress((0 / 7f * 100).toInt(), true)
        }else {
            progressBar.progress = (0 / 7f * 100).toInt()
        }
    }

    private fun setCalendarOptionLy() {
        calendarOptionLy.visibility = View.VISIBLE
        diaryLy.visibility = View.GONE

        optionTitleText.text = str(R.string.init_setting_script_1)

        osCalendarBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
            } else {
                showOsCalendarDialog()
            }
        }

        nextBtn.setOnClickListener {
            if(doubleTabFlag) {
                doubleTabFlag = false
                hideScript()
                initSettingLy.postDelayed({
                    setLastSettingLy()
                    showScript()
                }, 1000)
            }
        }
    }

    private fun showOsCalendarDialog() {
        showDialog(OsCalendarDialog(this) { result -> if(result) {
            val size = OsCalendarManager.getConnectedCalendarIdsSet().size
            osCalendarText.text = if(size == 0) {
                osCalendarText.setTextColor(AppTheme.disableText)
                ""
            } else {
                osCalendarText.setTextColor(AppTheme.secondaryText)
                String.format(str(R.string.referencing), size)
            }
        }}, true, true, true, false)
    }

    private fun setLastSettingLy() {
        lastSettingLy.visibility = View.VISIBLE
        calendarOptionLy.visibility = View.GONE

        optionTitleText.text = str(R.string.last_setting_script)

        nextBtn.setOnClickListener {
            if(doubleTabFlag) {
                doubleTabFlag = false
                hideScript()

                val realm = Realm.getDefaultInstance()
                val appUser = realm.where(AppUser::class.java).findFirst()
                if(appUser != null) {
                    realm.executeTransaction {
                        appUser.motto = mottoEdit.text.toString()
                    }
                }else {
                    realm.executeTransaction {
                        it.createObject(AppUser::class.java, FirebaseAuth.getInstance().uid)?.apply {
                            motto = mottoEdit.text.toString()
                        }
                    }
                }
                realm.close()

                initSettingLy.postDelayed({
                    startFinishLy()
                }, 1000)
            }
        }
    }

    private fun startFinishLy() {
        loginLy.visibility = View.VISIBLE
        loginBtn.visibility = View.GONE
        loginBackupText.visibility = View.GONE
        loadingText.visibility = View.VISIBLE
        loadingView.visibility = View.VISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(loginLy, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(loginLy, "translationY", dpToPx(15f), 0f))
        animSet.duration = 1000
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        val handler =  Handler()
        handler.postDelayed({
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(loginLy, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(loginLy, "translationY", 0f, dpToPx(15f)))
            animSet.duration = 500
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
            handler.postDelayed({
                startMainActivity()
            }, 500)
        }, 5000)
    }

    private fun showScript() {
        doubleTabFlag = true
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(initSettingLy, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(initSettingLy, "translationY", dpToPx(15f), 0f))
        animSet.duration = 500
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    private fun hideScript() {
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(initSettingLy, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(initSettingLy, "translationY", 0f, dpToPx(15f)))
        animSet.duration = 500
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    private fun loginRealm(account: GoogleSignInAccount) {
        val credentials = SyncCredentials.usernamePassword(account.email, account.id, false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                l("[로그인 실패]"+error?.errorCode?.intValue())
                hideProgressDialog()
            }
            override fun onSuccess(result: SyncUser?) {
                l("[로그인 성공]")
                result?.let { initRealm(it) }
            }
        })
        showProgressDialog(getString(R.string.plz_wait))
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("358462425934-h4nqspsj9ujlh747amcgc65arehf4kvu.apps.googleusercontent.com")
                .requestEmail()
                .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            startActivityForResult(GoogleSignIn.getClient(this, gso).signInIntent, 0)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        showProgressDialog(null)
        val mAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        l("signInWithCredential:success")
                        Prefs.putLong("last_backup_time", System.currentTimeMillis())
                        val user = mAuth.currentUser
                        val ref = FirebaseStorage.getInstance().reference
                                .child("${user?.uid}/db")
                        val realm = Realm.getDefaultInstance()
                        ref.getFile(File(realm.path)).addOnSuccessListener {
                            hideProgressDialog()
                            realm.close()
                            startMainActivity()
                        }.addOnFailureListener {
                            hideProgressDialog()
                            realm.close()
                            startCustomSettings()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        l("signInWithCredential:failure")
                    }
                }
    }

    private fun initRealm(result: SyncUser) {
        val config = result.createConfiguration(USER_URL)
                .fullSynchronization()
                .waitForInitialRemoteData()
                .build()
        realmAsyncTask = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                if (isDestroyed) {
                    realm.close()
                } else {
                    l("Realm 준비 완료")
                    Realm.setDefaultConfiguration(config)
                    hideProgressDialog()
                    startMainActivity()
                }
            }

            override fun onError(exception: Throwable) {
                super.onError(exception)
                hideProgressDialog()
            }
        })
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            intent.extras?.let { putExtras(it) }
        })
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.READ_CALENDAR
                                && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showOsCalendarDialog() }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(account) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace()
                l("Google sign in failed")
            }
        }
    }

    override fun onBackPressed() {}
}