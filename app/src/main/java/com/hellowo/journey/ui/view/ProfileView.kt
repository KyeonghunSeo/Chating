package com.hellowo.journey.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
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
    companion object

    var viewMode = ViewMode.CLOSED
    val menuWidth = dpToPx(280f)
    val headerHeight = dpToPx(80f)
    val gapWidth = dpToPx(15f)

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
        MainActivity.getMainPanel()?.let { mainPanel ->
            val panelOffset = -menuWidth * 0.7f
            val inboxView = MainActivity.getInboxView()
            val profileBtn = MainActivity.getProfileBtn()
            val addBtn = MainActivity.getMainAddBtn()
            vibrate(context)
            mainPanel.pivotX = 0f
            inboxView?.pivotX = 0f
            profileBtn?.pivotX = (profileBtn?.width?.toFloat() ?: 0f) * 0.8f
            profileBtn?.pivotY = 0f
            profileBtn?.setOnClickListener {
                MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
            }
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(mainPanel, "scaleX", 1f, 0.7f),
                    ObjectAnimator.ofFloat(mainPanel, "scaleY", 1f, 0.7f),
                    ObjectAnimator.ofFloat(mainPanel, "translationX", 0f, panelOffset),
                    ObjectAnimator.ofFloat(inboxView, "scaleX", 1f, 0.7f),
                    ObjectAnimator.ofFloat(inboxView, "scaleY", 1f, 0.7f),
                    ObjectAnimator.ofFloat(inboxView, "translationX", 0f, panelOffset + gapWidth),
                    ObjectAnimator.ofFloat(inboxView, "translationY", 0f, gapWidth),
                    ObjectAnimator.ofFloat(profileBtn, "scaleX", 1f, 3f),
                    ObjectAnimator.ofFloat(profileBtn, "scaleY", 1f, 3f),
                    ObjectAnimator.ofFloat(profileBtn, "translationY", 0f, headerHeight),
                    ObjectAnimator.ofFloat(addBtn, "translationY", 0f, headerHeight))
            animSet.duration = ANIM_DUR
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        MainActivity.getMainPanel()?.let { mainPanel ->
            val panelOffset = -menuWidth * 0.7f
            val inboxView = MainActivity.getInboxView()
            val profileBtn = MainActivity.getProfileBtn()
            val addBtn = MainActivity.getMainAddBtn()
            profileBtn?.setOnClickListener { show() }
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(mainPanel, "scaleX", 0.7f, 1f),
                    ObjectAnimator.ofFloat(mainPanel, "scaleY", 0.7f, 1f),
                    ObjectAnimator.ofFloat(mainPanel, "translationX", panelOffset, 0f),
                    ObjectAnimator.ofFloat(inboxView, "scaleX", 0.7f, 1f),
                    ObjectAnimator.ofFloat(inboxView, "scaleY", 0.7f, 1f),
                    ObjectAnimator.ofFloat(inboxView, "translationX", panelOffset + gapWidth, 0f),
                    ObjectAnimator.ofFloat(inboxView, "translationY", gapWidth, 0f),
                    ObjectAnimator.ofFloat(profileBtn, "scaleX", 3f, 1f),
                    ObjectAnimator.ofFloat(profileBtn, "scaleY", 3f, 1f),
                    ObjectAnimator.ofFloat(profileBtn, "translationY", headerHeight, 0f),
                    ObjectAnimator.ofFloat(addBtn, "translationY", headerHeight, 0f))
            animSet.duration = ANIM_DUR
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}