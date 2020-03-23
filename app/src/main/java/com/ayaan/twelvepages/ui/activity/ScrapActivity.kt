package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.bumptech.glide.Glide
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_scrap.*
import org.json.JSONObject
import java.util.*


class ScrapActivity : BaseActivity() {
    lateinit var realm: Realm
    var webData: MetaData? = null
    var text: String? = null
    var subject: String? = null

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)
        rootLy.layoutParams.width = AppStatus.screenWidth
        rootLy.layoutParams.height = AppStatus.screenHeight
        setGlobalTheme(rootLy)
        val intent = intent

        if (intent.action != null && intent.action == Intent.ACTION_SEND) {
//            for (key in intent.extras!!.keySet()) {
//                val value = intent.extras!!.get(key)
//                //l("intent send extra : " + String.format("%s / %s (%s)", key, value!!.toString(), value.javaClass.name))
//            }

            text = intent.getStringExtra(Intent.EXTRA_TEXT)
            subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

            if(text != null) {
                if (text!!.startsWith("http")) {
                    progressBar.visibility = View.VISIBLE
                    contentLy.visibility = View.GONE
                    val richPreview = RichPreview(object : ResponseListener {
                        override fun onData(metaData: MetaData) {
                            webData = metaData
                            TransitionManager.beginDelayedTransition(rootLy, makeFromBottomSlideTransition())
                            contentLy.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            if(!metaData.imageurl.isNullOrBlank()) {
                                linkImg.layoutParams.width = dpToPx(150)
                                linkImg.layoutParams.height = dpToPx(100)
                                linkImg.scaleType = ImageView.ScaleType.CENTER_CROP
                                linkImg.setPadding(0, 0, 0, 0)
                                linkImg.requestLayout()
                                Glide.with(this@ScrapActivity).load(metaData.imageurl).into(linkImg)
                            }else if(!metaData.favicon.isNullOrBlank()) {
                                linkImg.layoutParams.width = dpToPx(40)
                                linkImg.layoutParams.height = dpToPx(40)
                                linkImg.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                linkImg.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                                linkImg.requestLayout()
                                Glide.with(this@ScrapActivity).load(metaData.favicon).into(linkImg)
                            }
                            else {
                                linkImg.layoutParams.width = dpToPx(40)
                                linkImg.layoutParams.height = dpToPx(40)
                                linkImg.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                linkImg.setColorFilter(AppTheme.icon)
                                linkImg.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                                linkImg.requestLayout()
                                Glide.with(this@ScrapActivity).load(R.drawable.website).into(linkImg)
                            }
                            linkText.text = metaData.title
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                            titleText.text = str(R.string.scrap_text)
                            linkText.text = text
                            contentLy.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                        }
                    })
                    richPreview.getPreview(text)
                }else {
                    titleText.text = str(R.string.scrap_text)
                    linkText.text = text
                    progressBar.visibility = View.GONE
                }

                /*
                val config = SyncUser.current()
                        .createConfiguration(USER_URL)
                        .fullSynchronization()
                        .build()
                Realm.getInstanceAsync(config, object : Realm.Callback() {
                    override fun onSuccess(db: Realm) {
                        Realm.setDefaultConfiguration(config)
                        realm = db
                        val items = ArrayList<Folder>()
                        items.addAll(realm.where(Folder::class.java).sort("order", Sort.ASCENDING).findAll())
                        folderListView.layoutManager = LinearLayoutManager(this@ScrapActivity, HORIZONTAL, false)
                        folderListView.adapter = FolderAdapter(this@ScrapActivity, items)

                        saveTodayBtn.setOnClickListener { saveTimeObject(makeTimeObject(null, System.currentTimeMillis())) }
                        selectDateBtn.setOnClickListener {
                            showDialog(DatePickerDialog(this@ScrapActivity, System.currentTimeMillis()) {
                                saveTimeObject(makeTimeObject(null, it))
                            }, true, true, true, false)
                        }
                    }
                })
                */
                realm = Realm.getDefaultInstance()
                saveTodayBtn.setOnClickListener {
                    realm.where(Folder::class.java).equalTo("id", "calendar").findFirst()?.let {
                        saveTimeObject(makeTimeObject(it, System.currentTimeMillis()))
                        finish()
                    }
                }
                saveAndOpenBtn.setOnClickListener {
                    realm.where(Folder::class.java).equalTo("id", "calendar").findFirst()?.let {
                        val record = makeTimeObject(it, System.currentTimeMillis())
                        saveTimeObject(record)
                        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                            val bundle = Bundle()
                            bundle.putString("recordId", record.id)
                            if(MainActivity.instance == null) {
                                intent.putExtra("action", 2)
                                intent.putExtra("bundle", bundle)
                            }else {
                                MainActivity.instance?.playAction(2, bundle)
                            }
                            intent.addCategory(Intent.CATEGORY_LAUNCHER)
                            startActivity(intent)
                        }
                        finish()
                    }
                }
            }
        } else {
            Toast.makeText(this, R.string.invalid_info, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveTimeObject(record: Record) {
        RecordManager.save(record)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
    }

    private fun makeTimeObject(folder: Folder?, time: Long) : Record {
        val timeObject = Record()
        if(time != Long.MIN_VALUE) {
            timeObject.dtStart = getCalendarTime0(time)
            timeObject.dtEnd = getCalendarTime23(time)
        }
        timeObject.folder = folder
        timeObject.type = 0
        timeObject.setFormula(RecordCalendarAdapter.Formula.DOT)
        if (webData != null) {
            timeObject.title = subject ?: text
            webData?.let {
                timeObject.description = it.description
                val properties = JSONObject()
                properties.put("url", text)
                properties.put("imageurl", it.imageurl)
                properties.put("favicon", it.favicon)
                timeObject.links.add(Link(UUID.randomUUID().toString(), Link.Type.WEB.ordinal,
                        it.title, text, it.imageurl, it.favicon))
                return@let
            }
        }else {
            timeObject.title = null
            timeObject.description = "${if(subject.isNullOrEmpty()) "" else "$subject\n"}${if(text.isNullOrEmpty()) "" else text}"
        }
        return timeObject
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
