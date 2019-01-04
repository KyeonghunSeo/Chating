package com.hellowo.journey.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TemplateEditAdapter
import com.hellowo.journey.adapter.util.TemplateDiffCallback
import com.hellowo.journey.l
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.*
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_edit_template.*
import java.util.*

class TemplateEditActivity : BaseActivity() {
    private val realm = Realm.getDefaultInstance()
    private var templateList : RealmResults<Template>? = null
    private val items = ArrayList<Template>()
    private val newItmes = ArrayList<Template>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_template)
        initTheme(rootLy)
        backBtn.setOnClickListener { finish() }
        addBtn.setOnClickListener { _ ->
            showDialog(TypePickerDialog(this@TemplateEditActivity, TimeObject.Type.EVENT) { type ->
                realm.executeTransaction { it ->
                    var id = realm.where(Template::class.java).max("id")?.toInt()?.plus(1)
                    if(id == null) {
                        id = 0
                    }
                    val template = realm.createObject(Template::class.java, id)
                    template.title = getString(type.titleId)
                    template.order = id
                    template.type = type.ordinal
                }
                recyclerView.post { recyclerView.smoothScrollToPosition(items.size) }
            }, true, true, true, false)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TemplateEditAdapter(this, items){ action, template ->
            when(action) {
                -1 -> {
                    showDialog(CustomDialog(this@TemplateEditActivity,
                            template.title ?: "" ,
                            getString(R.string.delete_template), null) { result, _, _ ->
                        if(result) {
                            realm.executeTransaction { _ ->
                                realm.where(Template::class.java).equalTo("id", template.id)
                                        .findFirst()?.deleteFromRealm()
                            }
                        }else {
                            (recyclerView.adapter as TemplateEditAdapter).notifyItemChanged(items.indexOf(template))
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
                    }, true, true, true, false)
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
                3 -> {
                    val dialog = CustomDialog(this@TemplateEditActivity,
                            getString(R.string.template_title), null, null) { result, _, title ->
                        if(result) {
                            realm.executeTransaction { _ ->
                                realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                                    it.title = title
                                }
                            }
                        }
                    }
                    showDialog(dialog, true, true, true, false)
                    dialog.showInput(getString(R.string.template_title), template.title ?: "")
                }
                4 -> {
                    showDialog(TypePickerDialog(this@TemplateEditActivity, TimeObject.Type.values()[template.type]) { type ->
                        realm.executeTransaction { it ->
                            realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                                it.type = type.ordinal
                                it.style = 0
                            }
                        }
                    }, true, true, true, false)
                }
                5 -> {
                    showDialog(StylePickerDialog(this@TemplateEditActivity, template.colorKey, template.type, "") { style ->
                        realm.executeTransaction { it ->
                            realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
                                it.style = style
                            }
                        }
                    }, true, true, true, false)
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