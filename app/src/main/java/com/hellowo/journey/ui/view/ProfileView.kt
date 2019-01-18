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
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.SettingsActivity
import com.hellowo.journey.ui.dialog.OsCalendarDialog
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
        profileImage.setOnClickListener {
            MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
        }
        settingsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, SettingsActivity::class.java)) } }
        FirebaseAuth.getInstance().currentUser?.photoUrl?.let {
            Glide.with(this).load(it)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        if(appUser.profileImgUrl?.isNotEmpty() == true) {
            profileImage.clearColorFilter()
            Glide.with(this).load(appUser.profileImgUrl)
                    //.apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(dpToPx(25))).override(dpToPx(50)))
                    .into(profileImage)
        }else {
            profileImage.setColorFilter(AppTheme.primaryText)
        }
    }

    fun checkOsCalendarPermission() {
        MainActivity.instance?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
            } else {
                showDialog(OsCalendarDialog(it), true, true, true, false)
            }
        }
    }

    fun show() {
        visibility = View.VISIBLE
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        visibility = View.GONE
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}