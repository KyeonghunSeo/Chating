package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
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
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.ui.dialog.OsCalendarDialog
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import io.realm.*
import kotlinx.android.synthetic.main.activity_welcome.*
import java.io.File


class WelcomeActivity : BaseActivity() {
    private var realmAsyncTask: RealmAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initTheme(rootLy)
        loginBtn.setOnClickListener { signInWithGoogle() }
        callAfterViewDrawed(rootLy, Runnable{
            if(FirebaseAuth.getInstance().currentUser == null) {
                startShow()
            }else {
                startMainActivity()
            }
        })
    }

    private fun startShow() {
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
    var maxCount = 0
    var diaryNum = 0

    private fun setDiaryLy() {
        val user = FirebaseAuth.getInstance().currentUser
        optionTitleText.text = String.format(getString(R.string.init_setting_script_0), user?.displayName)
        maxCount = 0
        diaryLy.visibility = View.VISIBLE
        prevBtn.visibility = View.GONE
        nextBtn.setOnClickListener {
            if(doubleTabFlag) {
                doubleTabFlag = false
                hideScript()
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
                    startFinishLy()
                }, 1000)
            }
        }
    }

    private fun showOsCalendarDialog() {
        showDialog(OsCalendarDialog(this) { result -> if(result) {
            val size = OsCalendarManager.getConnectedCalendarIdsSet().size
            osCalendarText.text = if(size == 0) {
                osCalendarText.setTextColor(AppTheme.disableText)
                str(R.string.no_reference)
            } else {
                osCalendarText.setTextColor(AppTheme.primaryText)
                String.format(str(R.string.referencing), size)
            }
        }}, true, true, true, false)
    }

    private fun startFinishLy() {
        loginLy.visibility = View.VISIBLE
        loginBtn.visibility = View.GONE
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
        }, 3000)
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
}