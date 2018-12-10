package com.hellowo.journey.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.LinearLayout.HORIZONTAL
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.R
import com.hellowo.journey.adapter.FolderAdapter
import com.hellowo.journey.l
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Link
import com.hellowo.journey.model.TimeObject
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_scrap.*
import org.json.JSONObject
import java.util.*

class ScrapActivity : Activity() {
    lateinit var realm: Realm

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

            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

            if(text != null) {
                realm = Realm.getDefaultInstance()
                val items = ArrayList<Folder>()
                items.addAll(realm.where(Folder::class.java).sort("order", Sort.ASCENDING).findAll())
                folderListView.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
                folderListView.adapter = FolderAdapter(this, items) { action, folder ->
                    if(folder != null) {
                        val timeObject = TimeObject()
                        timeObject.folder = folder
                        timeObject.type = TimeObject.Type.NOTE.ordinal
                        timeObject.title = subject ?: text
                        if (text.startsWith("http") && subject != null) {
                            val properties = JSONObject()
                            properties.put("url", text)
                            timeObject.links.add(Link(UUID.randomUUID().toString(), Link.Type.WEB.ordinal,
                                    subject, properties.toString(), null))
                        }
                        TimeObjectManager.save(timeObject)
                        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        } else {
            Toast.makeText(this, R.string.invalid_info, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
