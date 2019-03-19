package com.hellowo.journey.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout.HORIZONTAL
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.adapter.FolderAdapter
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_scrap.*
import org.json.JSONObject
import java.util.*
import android.R.attr.data
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.hellowo.journey.*
import com.hellowo.journey.ui.dialog.DatePickerDialog
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
                                Glide.with(this@ScrapActivity).load(R.drawable.sharp_language_black_48dp).into(linkImg)
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
                        folderListView.adapter = FolderAdapter(this@ScrapActivity, items) { action, folder ->
                            saveTimeObject(makeTimeObject(folder, Long.MIN_VALUE))
                        }

                        saveTodayBtn.setOnClickListener { saveTimeObject(makeTimeObject(null, System.currentTimeMillis())) }
                        selectDateBtn.setOnClickListener { _ ->
                            showDialog(DatePickerDialog(this@ScrapActivity, System.currentTimeMillis()) {
                                saveTimeObject(makeTimeObject(null, it))
                            }, true, true, true, false)
                        }
                    }
                })
            }
        } else {
            Toast.makeText(this, R.string.invalid_info, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveTimeObject(timeObject: TimeObject) {
        TimeObjectManager.save(timeObject)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun makeTimeObject(folder: Folder?, time: Long) : TimeObject {
        val timeObject = TimeObject()
        if(time != Long.MIN_VALUE) {
            timeObject.dtStart = time
            timeObject.dtEnd = time
        }
        timeObject.folder = folder
        timeObject.type = TimeObject.Type.NOTE.ordinal
        timeObject.title = subject ?: text
        if (webData != null) {
            webData?.let {
                timeObject.description = it.description
                val properties = JSONObject()
                properties.put("url", text)
                properties.put("imageurl", it.imageurl)
                properties.put("favicon", it.favicon)
                timeObject.links.add(Link(UUID.randomUUID().toString(), Link.Type.WEB.ordinal,
                        it.title, properties.toString()))
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
