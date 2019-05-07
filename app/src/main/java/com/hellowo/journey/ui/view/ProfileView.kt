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

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)

        profileImage.setOnClickListener {
            MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
        }

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
            vibrate(context)
            val xOffset = -mainPanel.width * 0.7f * 0.5f
            mainPanel.pivotX = 0f
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(mainPanel, "scaleX", 1f, 0.7f),
                    ObjectAnimator.ofFloat(mainPanel, "scaleY", 1f, 0.7f),
                    ObjectAnimator.ofFloat(mainPanel, "translationX", 0f, xOffset))
            animSet.duration = ANIM_DUR
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        MainActivity.getMainPanel()?.let { mainPanel ->
            val xOffset = -mainPanel.width * 0.7f * 0.5f
            mainPanel.pivotX = 0f
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(mainPanel, "scaleX", 0.7f, 1f),
                    ObjectAnimator.ofFloat(mainPanel, "scaleY", 0.7f, 1f),
                    ObjectAnimator.ofFloat(mainPanel, "translationX", xOffset, 0f))
            animSet.duration = ANIM_DUR
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}