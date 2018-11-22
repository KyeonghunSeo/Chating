package com.hellowo.journey.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TemplateEditAdapter
import com.hellowo.journey.adapter.util.TemplateDiffCallback
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.Template
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.ColorPickerDialog
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.ui.dialog.TagDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_normal_list.*
import java.util.*

class TemplateEditActivity : AppCompatActivity() {
    private val realm = Realm.getDefaultInstance()
    private var templateList : RealmResults<Template>? = null
    private val items = ArrayList<Template>()
    private val newItmes = ArrayList<Template>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_list)

        titleText.text = getString(R.string.edit_template)
        backBtn.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TemplateEditAdapter(items){ action, template ->
            when(action) {
                -1 -> {
                    showDialog(CustomDialog(this@TemplateEditActivity, template.title ?: ""
                            , getString(R.string.delete_template), null) { result, _ ->
                        if(result) {
                            realm.executeTransaction { _ ->
                                realm.where(Template::class.java).equalTo("id", template.id)
                                        .findFirst()?.deleteFromRealm()
                            }
                        }else {
                            (recyclerView.adapter as TemplateEditAdapter).notifyDataSetChanged()
                        }
                    }, true, true, true, false)
                }
                0 -> {
                    showDialog(ColorPickerDialog(this@TemplateEditActivity, template.colorKey) { colorKey, fontColor ->
                        realm.executeTransaction { it ->
                            realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                                it.colorKey = colorKey
                                it.fontColor = fontColor
                            }
                        }
                    }, true, false, true, false)
                }
                1 -> {
                    val items = ArrayList<Tag>().apply { addAll(template.tags) }
                    showDialog(TagDialog(this@TemplateEditActivity, items) { tags ->
                        realm.executeTransaction { _ ->
                            realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                                it.tags.clear()
                                it.tags.addAll(tags)
                            }
                        }
                    }, true, true, true, false)
                }
                2 -> {
                    realm.executeTransaction { _ ->
                        realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                            it.inCalendar = !it.inCalendar
                        }
                    }
                }
            }

        }
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        templateList = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAllAsync()
        templateList?.addChangeListener { result, changeSet ->
            if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                items.clear()
                result.forEach { items.add(realm.copyFromRealm(it)) }
                adapter.notifyDataSetChanged()
            }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                newItmes.clear()
                result.forEach { newItmes.add(realm.copyFromRealm(it)) }
                Thread {
                    val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItmes))
                    items.clear()
                    items.addAll(newItmes)
                    Handler(Looper.getMainLooper()).post{
                        diffResult.dispatchUpdatesTo(adapter)
                    }
                }.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        templateList?.removeAllChangeListeners()
        realm.close()
    }
}