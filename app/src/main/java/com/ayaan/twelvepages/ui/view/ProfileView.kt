package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
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
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.dialog.InputDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.view_profile.view.*
import kotlinx.android.synthetic.main.view_saerch.view.*
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val scale = 0.7f
    private val animDur = 300L
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
        syncBtn.setOnClickListener { sync() }
    }

    fun updateUserUI(appUser: AppUser) {
        l("[프로필 뷰 갱신]")
        val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl
        when {
            photoUrl != null && photoUrl.toString().startsWith("https://firebasestorage.googleapis.com") ->
                Glide.with(this).load(photoUrl)
                        .apply(RequestOptions().override(dpToPx(150)))
                        .into(profileImg)
            else -> Glide.with(this).load(R.drawable.default_profile)
                    .apply(RequestOptions().override(dpToPx(150)))
                    .into(profileImg)
        }

        nameText.text = FirebaseAuth.getInstance().currentUser?.displayName
        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        if(appUser.motto?.isNotBlank() == true) {
            mottoText.typeface = ResourcesCompat.getFont(context, R.font.regular_s)
            mottoText.text = appUser.motto
        }

        if(AppStatus.isPremium()) {
            premiumText.visibility = View.VISIBLE
            premiumImg.alpha = 1f
        }else {
            premiumText.visibility = View.GONE
            premiumImg.alpha = 0.3f
        }
    }

    private fun sync() {
        MainActivity.instance?.let { activity ->
            activity.showProgressDialog()
            val mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser
            val ref = FirebaseStorage.getInstance().reference
                    .child("${user?.uid}/db")
//        val realm = Realm.getDefaultInstance()
            ref.metadata.addOnSuccessListener {
                activity.hideProgressDialog()
                val dialog = CustomDialog(activity, activity.getString(R.string.sync),
                        AppDateFormat.ymdkey.format(Date(it.updatedTimeMillis)), null,
                        R.drawable.download_cloud) { result, _, _ ->
                    if(result) {

                    }
                }
                showDialog(dialog, true, true, true, false)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                toast(R.string.no_cloud_data)
            }
//        ref.getFile(File(realm.path)).addOnSuccessListener {
//            realm.close()
//        }.addOnFailureListener {
//            MainActivity.instance?.hideProgressDialog()
//            realm.close()
//        }
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

    private fun setGift() {
        if(!Prefs.getBoolean("isTakeShareGift", false)) {
            instaBtn.visibility = View.GONE
            giftLy.visibility = View.VISIBLE
            giftLy.setOnClickListener {
                MainActivity.instance?.let {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, str(R.string.app_share_text))
                    shareIntent.type = "text/plain"
                    val chooser = Intent.createChooser(shareIntent, str(R.string.app_name))
                    it.startActivityForResult(chooser, RC_APP_SHARE)
                }
            }
        }else {
            instaBtn.visibility = View.GONE
            giftLy.visibility = View.GONE
            instaBtn.setOnClickListener {
                MainActivity.instance?.let {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://www.instagram.com/moon_records_diary")
                    it.startActivity(intent)
                }
            }
        }
    }

    fun show() {
        startAnalytics()
        versionCheck()
        setGift()

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