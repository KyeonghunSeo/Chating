package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.firebase.auth.FirebaseAuth
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.ui.activity.AboutUsActivity
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.PremiumActivity
import com.ayaan.twelvepages.ui.activity.SettingsActivity
import com.ayaan.twelvepages.ui.dialog.InputDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.view_profile.view.*
import kotlinx.android.synthetic.main.view_saerch.view.*
import java.util.*
import kotlin.collections.HashMap

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val animDur = 300L
    private val zOffset = dpToPx(30f)
    private val panelOffset = dpToPx(170f)
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
        profileImg.setOnClickListener { MainActivity.instance?.checkExternalStoragePermission(RC_PROFILE_IMAGE) }
        settingsBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivityForResult(Intent(it, SettingsActivity::class.java), RC_SETTING) } }
        premiumTag.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, PremiumActivity::class.java)) } }
        aboutUsBtn.setOnClickListener { MainActivity.instance?.let { it.startActivity(Intent(it, AboutUsActivity::class.java)) } }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]" + appUser.id)
        when {
            FirebaseAuth.getInstance().currentUser?.photoUrl != null ->
                Glide.with(this).load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                        .apply(RequestOptions().override(dpToPx(150)))
                        .into(profileImg)
            else -> profileImg.setImageResource(R.drawable.profile)
        }

        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.motto?.isNotBlank() == true) {
            mottoText.typeface = ResourcesCompat.getFont(context, R.font.regular_s)
            mottoText.text = appUser.motto
        }
        if(AppStatus.isPremium()) {
            premiumImg.clearColorFilter()
        }else {
            setImageViewGrayFilter(premiumImg)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun startAnalytics() {
        object : AsyncTask<String, String, String?>() {
            var totalCount = 0L
            val tagStr = StringBuilder()
            val firstRecordStr = StringBuilder()
            val lastRecordStr = StringBuilder()

            override fun onPreExecute() {
                historyLy.setOnClickListener {
                    if(moreHistoryLy.visibility == View.VISIBLE) {
                        moreHistoryLy.visibility = View.GONE
                        moreText.visibility = View.VISIBLE
                    }else {
                        moreHistoryLy.visibility = View.VISIBLE
                        moreText.visibility = View.GONE
                    }
                }
                moreHistoryLy.visibility = View.GONE
                moreText.visibility = View.VISIBLE
            }

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

                ////////////////////////////////////////////////////////////////////////////////////

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

                tagStr.append("${String.format(str(R.string.total_tag), tagCounts.values.sum())}\n")

                sortedTags.forEach {
                    tagStr.append("#${it.key.title} : ${it.value}개\n")
                }

                ////////////////////////////////////////////////////////////////////////////////////

                val firstRecord = realm.where(Record::class.java)
                        .sort("dtUpdated", Sort.ASCENDING).findFirst()
                firstRecord?.let {
                    firstRecordStr.append("${App.context.getString(R.string.first_record_is)} " +
                            "${it.getShortTilte()} - " +
                            "${AppDateFormat.ymde.format(Date(it.dtUpdated))} " +
                            AppDateFormat.time.format(Date(it.dtUpdated)))
                }

                val lastRecord = realm.where(Record::class.java)
                        .sort("dtUpdated", Sort.DESCENDING).findFirst()
                lastRecord?.let {
                    lastRecordStr.append("${App.context.getString(R.string.last_record_is)} " +
                            "${it.getShortTilte()} - " +
                            "${AppDateFormat.ymde.format(Date(it.dtUpdated))} " +
                            AppDateFormat.time.format(Date(it.dtUpdated)))
                }

                realm.close()
                return null
            }
            override fun onProgressUpdate(vararg text: String) {}
            override fun onPostExecute(result: String?) {
                if(viewMode == ViewMode.OPENED) {
                    totalRecordsText.text = String.format(str(R.string.total_records), totalCount)
                    totalTagText.text = tagStr.trim()
                    firstRecordText.text = firstRecordStr
                    lastRecordText.text = lastRecordStr
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    var latestVer = ""

    @SuppressLint("SetTextI18n")
    private fun versionCheck() {
        if(latestVer.isBlank()) {
            val currentVersion = App.context.packageManager.getPackageInfo(App.context.packageName, 0).versionName
            versionText.text = currentVersion
            val db = FirebaseFirestore.getInstance()
            db.collection("version")
                    .document("latest")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            latestVer = document.data?.get("name").toString()
                            if(currentVersion == latestVer) {
                                versionText.setTextColor(AppTheme.disableText)
                                versionText.text = "${versionText.text} [${str(R.string.latest_ver)}]"
                            }else {
                                versionText.setTextColor(AppTheme.secondaryText)
                                versionText.text = "${versionText.text} [${str(R.string.need_update)}]"
                                versionText.setOnClickListener {
                                    latestVer = ""
                                    hide()
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/")))
                                }
                            }
                            l("DocumentSnapshot data: ${document.data}")
                        }
                    }
                    .addOnFailureListener {
                        l("Failed get version: ${it.message}")
                    }
        }
    }

    fun show() {
        startAnalytics()
        versionCheck()
        val animSet = AnimatorSet()
        val animList = ArrayList<Animator>()
        MainActivity.getMainPanel()?.let {
            animList.add(ObjectAnimator.ofFloat(it, "scaleX", 1f, scale))
            animList.add(ObjectAnimator.ofFloat(it, "scaleY", 1f, scale))
            animList.add(ObjectAnimator.ofFloat(it, "translationX", 0f, panelOffset))
            animList.add(ObjectAnimator.ofFloat(it, "radius", 0f, dpToPx(1f)))
        }
        animSet.playTogether(animList)
        animSet.duration = animDur
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        val animSet = AnimatorSet()
        val animList = ArrayList<Animator>()
        MainActivity.getMainPanel()?.let {
            animList.add(ObjectAnimator.ofFloat(it, "scaleX", it.scaleX, 1f))
            animList.add(ObjectAnimator.ofFloat(it, "scaleY", it.scaleY, 1f))
            animList.add(ObjectAnimator.ofFloat(it, "translationX", it.translationX, 0f))
            animList.add(ObjectAnimator.ofFloat(it, "radius", dpToPx(1f), 0f))
        }
        animSet.playTogether(animList)
        animSet.duration = animDur
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}