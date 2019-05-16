package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.model.AppUser
import com.hellowo.journey.ui.activity.AboutUsActivity
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.PremiumActivity
import com.hellowo.journey.ui.activity.SettingsActivity
import com.hellowo.journey.ui.dialog.InputDialog
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val profileCloseMargin = dpToPx(10)
    private val profileOpenMargin = dpToPx(14)
    private val profileOpenTopMargin = dpToPx(60)
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
        closeBtn.setOnClickListener { hide() }
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
        MainActivity.getProfileBtn()?.let { profileBtn ->
            profileBtn.setOnClickListener {
                MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE)
            }
            val transiion = makeChangeBounceTransition()
            transiion.duration = 300L
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
                    animList.add(ObjectAnimator.ofFloat(profileCard, "radius", profileCard.radius, dpToPx(0f)))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", 0f, panelOffset))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = 300L
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as FrameLayout.LayoutParams).let {
                it.width = profileBtnSize * 3
                it.height = profileBtnSize * 3
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
            transiion.duration = 300L
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
                    animList.add(ObjectAnimator.ofFloat(profileCard, "radius", profileCard.radius, dpToPx(16f)))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", it.scaleX, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", it.scaleY, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, 0f))
                        animList.add(ObjectAnimator.ofFloat(it, "translationY", it.translationY, 0f))
                        animList.add(ObjectAnimator.ofFloat(it, "elevation", it.elevation, zOffset))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = 300L
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as FrameLayout.LayoutParams).let {
                it.width = profileBtnSize
                it.height = profileBtnSize
                it.gravity = Gravity.RIGHT
                it.setMargins(0, profileCloseMargin, profileCloseMargin, 0)
            }
            requestLayout()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}