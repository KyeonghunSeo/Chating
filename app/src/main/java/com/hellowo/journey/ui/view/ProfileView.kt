package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.activity.AboutUsActivity
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.PremiumActivity
import com.hellowo.journey.ui.activity.SettingsActivity
import com.hellowo.journey.ui.dialog.CalendarSettingsDialog
import com.hellowo.journey.ui.dialog.InputDialog
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val headerHeight = dpToPx(80f)
    private val gapWidth = dpToPx(15f)
    private val zOffset = dpToPx(30f)
    private val panelOffset = -dpToPx(280f)
    private var targetPanel: CardView? = null
    private var subPanel: CardView? = null
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
        mottoText.setOnClickListener {
            showDialog(InputDialog(context as Activity, context.getString(R.string.motto), null, null,
                    mottoText.text.toString()) { result, text ->
                if(result) { MainActivity.getViewModel()?.saveMotto(text) }
            }, true, true, true, false)
        }
        searchBtn.setOnClickListener { MainActivity.instance?.showSearchView() }
        settingsBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivityForResult(Intent(it, SettingsActivity::class.java), RC_SETTING) } }
        premiumBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, PremiumActivity::class.java)) } }
        aboutUsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, AboutUsActivity::class.java)) } }

        calendarBtn.setOnClickListener {
            adjustViews()
            hide()
        }

        inboxBtn.setOnClickListener {
            adjustViews()
            hide()
        }
    }

    fun initViews() {
        adjustViews()
        targetPanel?.cardElevation = zOffset
        subPanel?.cardElevation = zOffset - 1
        subPanel?.scaleX = scale
        subPanel?.scaleY = scale
    }

    fun adjustViews() {
        if(MainActivity.getViewModel()?.targetFolder?.value == null) {
            calendarText.setTextColor(AppTheme.primaryText)
            calendarBar.visibility = View.VISIBLE
            inboxText.setTextColor(AppTheme.disableText)
            inboxBar.visibility = View.GONE
            targetPanel = MainActivity.getMainPanel()
        }else {
            calendarText.setTextColor(AppTheme.disableText)
            calendarBar.visibility = View.GONE
            inboxText.setTextColor(AppTheme.primaryText)
            inboxBar.visibility = View.VISIBLE
            subPanel = MainActivity.getMainPanel()
        }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.motto?.isNotBlank() == true) {
            mottoText.text = appUser.motto
        }
    }

    fun show() {
        vibrate(context)
        MainActivity.instance?.let {
            val profileBtn = MainActivity.getProfileBtn()
            val profileCard = profileBtn?.findViewById<CardView>(R.id.profileCard)
            profileBtn?.pivotX = (profileBtn?.width?.toFloat() ?: 0f) * 0.8f
            profileBtn?.pivotY = 0f
            profileBtn?.setOnClickListener {
                MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
            }

            val animSet = AnimatorSet()
            val animList = ArrayList<Animator>()
            animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX", 1f, 3f))
            animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY", 1f, 3f))
            animList.add(ObjectAnimator.ofFloat(profileBtn, "translationY", 0f, headerHeight))
            animList.add(ObjectAnimator.ofFloat(profileCard, "radius", dpToPx(15f), 0f))

            targetPanel?.let {
                animList.add(ObjectAnimator.ofFloat(it, "scaleX", 1f, 0.7f))
                animList.add(ObjectAnimator.ofFloat(it, "scaleY", 1f, 0.7f))
                animList.add(ObjectAnimator.ofFloat(it, "translationX", 0f, panelOffset))
                return@let
            }
            subPanel?.let {
                animList.add(ObjectAnimator.ofFloat(it, "translationX", -width.toFloat(), panelOffset + gapWidth))
                animList.add(ObjectAnimator.ofFloat(it, "translationY", 0f, gapWidth))
                return@let
            }

            animSet.playTogether(animList)
            animSet.duration = 300L
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        MainActivity.instance?.let {
            val profileBtn = MainActivity.getProfileBtn()
            val profileCard = profileBtn?.findViewById<CardView>(R.id.profileCard)
            profileBtn?.setOnClickListener { show() }

            val animSet = AnimatorSet()
            val animList = ArrayList<Animator>()
            animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX", 3f, 1f))
            animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY", 3f, 1f))
            animList.add(ObjectAnimator.ofFloat(profileBtn, "translationY", headerHeight, 0f))
            animList.add(ObjectAnimator.ofFloat(profileCard, "radius", 0f, dpToPx(15f)))

            targetPanel?.let {
                animList.add(ObjectAnimator.ofFloat(it, "scaleX", it.scaleX, 1f))
                animList.add(ObjectAnimator.ofFloat(it, "scaleY", it.scaleY, 1f))
                animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, 0f))
                animList.add(ObjectAnimator.ofFloat(it, "translationY", it.translationY, 0f))
                animList.add(ObjectAnimator.ofFloat(it, "elevation", it.elevation, zOffset))
                return@let
            }
            subPanel?.let {
                animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, -width.toFloat()))
                animList.add(ObjectAnimator.ofFloat(it, "elevation", it.elevation, zOffset - 1))
                return@let
            }

            animSet.playTogether(animList)
            animSet.duration = 300L
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}