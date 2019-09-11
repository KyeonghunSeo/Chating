package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.HORIZONTAL
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayaan.twelvepages.adapter.FolderAdapter
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_scrap.*
import org.json.JSONObject
import java.util.*
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import io.realm.SyncUser


class ScrapActivity : Activity() {
    lateinit var realm: Realm
    var webData: MetaData? = null
    var text: String? = null
    var subject: String? = null

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)
        val intent = intent

        if (intent.action != null && intent.action == Intent.ACTION_SEND) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.get(key)
                l("intent send extra : " + String.format("%s / %s (%s)", key,
                        value!!.toString(), value.javaClass.name))
            }

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
                            linkLy.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            if(!metaData.imageurl.isNullOrBlank())
                                Glide.with(this@ScrapActivity).load(metaData.imageurl).into(linkImg)
                            else if(!metaData.favicon.isNullOrBlank())
                                Glide.with(this@ScrapActivity).load(metaData.favicon).into(linkImg)
                            else {
                                linkImg.setColorFilter(AppTheme.secondaryText)
                                Glide.with(this@ScrapActivity).load(R.drawable.website).into(linkImg)
                            }
                            linkText.text = metaData.title
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                            contentLy.visibility = View.VISIBLE
                            linkLy.visibility = View.GONE
                            progressBar.visibility = View.GONE
                        }
                    })
                    richPreview.getPreview(text)
                }else {
                    contentLy.visibility = View.VISIBLE
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
                    }
                }
                saveKeepBtn.setOnClickListener {
                    realm.where(Folder::class.java).equalTo("id", "keep").findFirst()?.let {
                        saveTimeObject(makeTimeObject(it, Long.MIN_VALUE))
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
        finish()
    }

    private fun makeTimeObject(folder: Folder?, time: Long) : Record {
        val timeObject = Record()
        if(time != Long.MIN_VALUE) {
            timeObject.dtStart = getCalendarTime0(time)
            timeObject.dtEnd = getCalendarTime23(time)
        }
        timeObject.folder = folder
        timeObject.type = 0
        timeObject.title = subject ?: text
        if (webData != null) {
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
            timeObject.description = text
        }
        return timeObject
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
