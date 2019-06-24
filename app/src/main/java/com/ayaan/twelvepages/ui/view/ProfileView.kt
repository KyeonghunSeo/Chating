package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import com.google.firebase.auth.FirebaseAuth
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.AboutUsActivity
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.PremiumActivity
import com.ayaan.twelvepages.ui.activity.SettingsActivity
import com.ayaan.twelvepages.ui.dialog.InputDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val animDur = 350L
    private val profileCloseTopMargin = dpToPx(15)
    private val profileCloseRightMargin = dpToPx(20)
    private val profileOpenMargin = dpToPx(7)
    private val profileOpenTopMargin = dpToPx(7)
    private val profileBtnSize = dpToPx(50)
    private val zOffset = dpToPx(30f)
    private val panelOffset = dpToPx(200f)
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
        mottoText.setOnClickListener {
            showDialog(InputDialog(context as Activity, context.getString(R.string.motto), null, null,
                    mottoText.text.toString(), false) { result, text ->
                if(result) { MainActivity.getViewModel()?.saveMotto(text) }
            }, true, true, true, false)
        }
        searchBtn.setOnClickListener { MainActivity.instance?.showSearchView() }
        settingsBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivityForResult(Intent(it, SettingsActivity::class.java), RC_SETTING) } }
        premiumBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, PremiumActivity::class.java)) } }
        premiumTag.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, PremiumActivity::class.java)) } }
        aboutUsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, AboutUsActivity::class.java)) } }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.motto?.isNotBlank() == true) {
            mottoText.text = appUser.motto
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun startAnalytics() {
        object : AsyncTask<String, String, String?>() {
            override fun doInBackground(vararg args: String): String? {
                val realm = Realm.getDefaultInstance()
                val totalSize = realm.where(Record::class.java).notEqualTo("dtCreated", -1L).count()
                l("totalSize : $totalSize")
                realm.close()
                return null
            }
            override fun onProgressUpdate(vararg text: String) {}
            override fun onPostExecute(result: String?) {
                if(viewMode == ViewMode.OPENED) {

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun show() {
        vibrate(context)
        startAnalytics()
        MainActivity.getProfileBtn()?.let { profileBtn ->
            profileBtn.pivotX = 0f
            profileBtn.pivotY = 0f
            profileBtn.setOnClickListener { MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE) }
            val transiion = makeChangeBounceTransition()
            transiion.duration = animDur
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX", 1f, 3.5f))
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY", 1f, 3.5f))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", 0f, panelOffset))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = animDur
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as LayoutParams).let {
                it.gravity = Gravity.LEFT
                it.setMargins(profileOpenMargin, profileOpenTopMargin, 0, 0)
            }
            requestLayout()
        }
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        MainActivity.getProfileBtn()?.let { profileBtn ->
            profileBtn.setOnClickListener { show() }
            val transiion = makeChangeBounceTransition()
            transiion.duration = animDur
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX", 3.5f, 1f))
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY", 3.5f, 1f))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", it.scaleX, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", it.scaleY, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, 0f))
                        animList.add(ObjectAnimator.ofFloat(it, "translationY", it.translationY, 0f))
                        animList.add(ObjectAnimator.ofFloat(it, "elevation", it.elevation, zOffset))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = animDur
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as LayoutParams).let {
                it.gravity = Gravity.RIGHT
                it.setMargins(0, profileCloseTopMargin, profileCloseRightMargin, 0)
            }
            requestLayout()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}