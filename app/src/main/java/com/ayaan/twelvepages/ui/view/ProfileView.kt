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
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.ui.activity.AboutUsActivity
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.PremiumActivity
import com.ayaan.twelvepages.ui.activity.SettingsActivity
import com.ayaan.twelvepages.ui.dialog.InputDialog
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.view_profile.view.*
import java.util.*
import kotlin.collections.HashMap

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val animDur = 300L
    private val profileCloseMargin = dpToPx(13)
    private val profileOpenTopMargin = dpToPx(60)
    private val profileOpenLeftMargin = dpToPx(22)
    private val profileCardRadius = dpToPx(12f)
    private val zOffset = dpToPx(30f)
    private val panelOffset = dpToPx(200f)
    private val profileViewScale = 2.5f
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true).let {
            setBackgroundColor(AppTheme.background)
        }
        mottoText.setOnClickListener {
            showDialog(InputDialog(context as Activity,
                    R.drawable.note,
                    context.getString(R.string.motto), null, null,
                    mottoText.text.toString(), false) { result, text ->
                if(result) { MainActivity.getViewModel()?.saveMotto(text) }
            }, true, true, true, false)
        }
        searchBtn.setOnClickListener { MainActivity.instance?.showSearchView() }
        settingsBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivityForResult(Intent(it, SettingsActivity::class.java), RC_SETTING) } }
        premiumTag.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, PremiumActivity::class.java)) } }
        aboutUsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, AboutUsActivity::class.java)) } }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]" + appUser.id)
        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.motto?.isNotBlank() == true) {
            mottoText.text = appUser.motto
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun startAnalytics() {
        object : AsyncTask<String, String, String?>() {
            var totalCount = 0L

            override fun doInBackground(vararg args: String): String? {
                val realm = Realm.getDefaultInstance()
                totalCount = realm.where(Record::class.java).notEqualTo("dtCreated", -1L).count()
                l("totalCount : $totalCount")

                val latestRecords = realm.where(Record::class.java)
                        .notEqualTo("dtCreated", -1L)
                        .sort("dtUpdated", Sort.DESCENDING)
                        .limit(3)
                        .findAll()

                latestRecords.forEach {
                    l("latest data -> $it")
                }

                val tagCounts = HashMap<Tag, Int>()
                realm.where(Tag::class.java).findAll().forEach {
                    val count = realm.where(Record::class.java)
                            .notEqualTo("dtCreated", -1L)
                            .equalTo("tags.id", it.id)
                            .count()
                    tagCounts[it] = count.toInt()
                    l("tag : ${it.title} : $count")
                }
                val sortedTags = tagCounts.toSortedMap(kotlin.Comparator { l, r ->
                    val countCompare = tagCounts[l]?.compareTo(tagCounts[r]?:0) ?: 0
                    if(countCompare == 0) {
                        return@Comparator l.order.compareTo(r.order)
                    }
                    return@Comparator countCompare * -1
                })

                l("sortedTags : ${sortedTags.keys.map { it.title }}")

                realm.close()
                return null
            }
            override fun onProgressUpdate(vararg text: String) {}
            override fun onPostExecute(result: String?) {
                if(viewMode == ViewMode.OPENED) {
                    totalRecordsText.text = String.format(str(R.string.total_records), totalCount)
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun show() {
        vibrate(context)
        startAnalytics()
        MainActivity.getProfileBtn()?.let { profileBtn ->
            val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
            profileBtn.setOnClickListener{ MainActivity.instance?.checkExternalStoragePermission(RC_PRFOFILE_IMAGE) }
            profileBtn.pivotX = 0f
            profileBtn.pivotY = 0f
            val transiion = makeChangeBounceTransition()
            transiion.duration = animDur
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX",  profileBtn.scaleX, profileViewScale))
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY",  profileBtn.scaleY, profileViewScale))
                    animList.add(ObjectAnimator.ofFloat(profileCard, "radius", profileCard.radius, dpToPx(25f)))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", 1f, scale))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", 0f, panelOffset))
                        animList.add(ObjectAnimator.ofFloat(it, "radius", 0f, dpToPx(6f)))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = animDur
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as LayoutParams).let {
                it.topMargin = profileOpenTopMargin
                it.leftMargin = profileOpenLeftMargin
            }
            (profileCard.layoutParams as LayoutParams).setMargins(0, 0, 0, 0)
            profileBtn.requestLayout()
            profileCard.requestLayout()
            requestLayout()
        }
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        MainActivity.getProfileBtn()?.let { profileBtn ->
            val profileCard = profileBtn.findViewById<CardView>(R.id.profileCard)
            profileBtn.setOnClickListener { show() }
            val transiion = makeChangeBounceTransition()
            transiion.duration = animDur
            transiion.setPathMotion(ArcMotion())
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    val animList = ArrayList<Animator>()
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleX", profileBtn.scaleX, 1f))
                    animList.add(ObjectAnimator.ofFloat(profileBtn, "scaleY", profileBtn.scaleY, 1f))
                    animList.add(ObjectAnimator.ofFloat(profileCard, "radius", profileCard.radius, profileCardRadius))
                    MainActivity.getMainPanel()?.let {
                        animList.add(ObjectAnimator.ofFloat(it, "scaleX", it.scaleX, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "scaleY", it.scaleY, 1f))
                        animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, 0f))
                        animList.add(ObjectAnimator.ofFloat(it, "radius", zOffset, 0f))
                    }
                    animSet.playTogether(animList)
                    animSet.duration = animDur
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(profileBtn, transiion)
            (profileBtn.layoutParams as LayoutParams).let {
                it.topMargin = 0
                it.leftMargin = dpToPx(1)
            }
            (profileCard.layoutParams as LayoutParams).setMargins(profileCloseMargin, profileCloseMargin, profileCloseMargin, profileCloseMargin)
            profileCard.requestLayout()
            requestLayout()
        }
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}