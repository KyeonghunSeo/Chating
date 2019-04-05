package com.hellowo.journey.ui.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.activity.AboutUsActivity
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.SettingsActivity
import com.hellowo.journey.ui.dialog.CalendarSettingsDialog
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
        contentLy.setBackgroundColor(AppTheme.backgroundColor)
        contentLy.visibility = View.GONE
        contentLy.setOnClickListener {}

        profileImage.setOnClickListener {
            MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
        }

        calendarSettingBtn.setOnClickListener {
            hide()
            showDialog(CalendarSettingsDialog(context as Activity),
                    true, false, true, false)
        }

        searchBtn.setOnClickListener { MainActivity.getViewModel()?.currentTab?.value = 2 }

        settingsBtn.setOnClickListener { _ -> MainActivity.instance?.let { it.startActivity(Intent(it, SettingsActivity::class.java)) } }
        aboutUsBtn.setOnClickListener { _ -> MainActivity.instance?.let { it.startActivity(Intent(it, AboutUsActivity::class.java)) } }
        FirebaseAuth.getInstance().currentUser?.photoUrl?.let {
            Glide.with(this).load(it)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }

    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.profileImgUrl?.isNotEmpty() == true) {
            Glide.with(this).load(appUser.profileImgUrl)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }
    }

    fun show() {
        val transitionSet = TransitionSet()
        val t1 = makeFromLeftSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.duration = 300L
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setBackgroundColor(AppTheme.primaryText)
        backgroundLy.setOnClickListener { hide() }
        backgroundLy.isClickable = true
        backgroundLy.visibility = View.VISIBLE
        contentLy.visibility = View.VISIBLE
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        val transitionSet = TransitionSet()
        val t1 = makeFromLeftSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.duration = 300L
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        backgroundLy.visibility = View.GONE
        contentLy.visibility = View.GONE
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}