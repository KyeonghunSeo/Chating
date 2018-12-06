package com.hellowo.journey.ui.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.hellowo.journey.*
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.SettingsActivity
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
        contentLy.visibility = View.INVISIBLE

        profileImage.setOnClickListener {
            MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
        }
        settingsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, SettingsActivity::class.java)) } }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        if(appUser.profileImg?.isNotEmpty() == true) {
            profileImage.clearColorFilter()
            Glide.with(this).load(appUser.profileImg)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }else {
            profileImage.setColorFilter(AppTheme.primaryText)
        }
    }

    private fun checkOsCalendarPermission() {
        MainActivity.instance?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
            } else {
                OsCalendarManager.getCalendarList(it)
            }
        }
    }

    fun show() {
        viewMode = ViewMode.OPENED
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.VISIBLE
        backgroundLy.setBackgroundColor(AppTheme.primaryText)
        backgroundLy.setOnClickListener { hide() }
        backgroundLy.isClickable = true
        contentLy.visibility = View.VISIBLE
    }

    fun hide() {
        viewMode = ViewMode.CLOSED
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.INVISIBLE
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        contentLy.visibility = View.INVISIBLE
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}