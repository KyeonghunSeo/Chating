package com.hellowo.journey.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TemplateEditAdapter
import com.hellowo.journey.adapter.util.TemplateDiffCallback
import com.hellowo.journey.l
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.Template
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
    private val template = Template()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_template)
        initTheme(rootLy)
        backBtn.setOnClickListener { onBackPressed() }
        deleteBtn.setOnClickListener {
            showDialog(CustomDialog(this@TemplateEditActivity, template.title ?: "" ,
                    getString(R.string.delete_template), null) { result, _, _ ->
                if(result) { delete() }
            }, true, true, true, false)
        }

        if(!intent.getStringExtra("id").isNullOrEmpty()) {
            realm.where(Template::class.java).equalTo("id", intent.getStringExtra("id"))
                    .findFirst()?.let { template.copy(it) }
        }else {
            template.folder = MainActivity.getTargetFolder()
        }
        l(template.toString())

        updateColorUI()

        /*
        addBtn.setOnClickListener {
            realm.executeTransaction {
                var id = realm.where(Template::class.java).max("id")?.toInt()?.plus(1)
                if(id == null) {
                    id = 0
                }
                val template = realm.createObject(Template::class.java, id)
                template.title = ""
                template.order = id
                template.type = 0
            }
            recyclerView.post { recyclerView.smoothScrollToPosition(items.size) }
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
                            it.isInCalendar() = !it.isInCalendar()
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

                }
                5 -> {
                    showDialog(StylePickerDialog(this@TemplateEditActivity, template.colorKey, template.type, "") { style ->
                        realm.executeTransaction {
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

        */
    }

    private fun updateColorUI() {
        colorImg.setColorFilter(AppTheme.getColor(template.colorKey))
        colorBtn.setOnClickListener {
            showDialog(ColorPickerDialog(this@TemplateEditActivity, template.colorKey) { colorKey ->
                template.colorKey = colorKey
                updateColorUI()
            }, true, true, true, false)
        }
    }

    private fun confirm() {
        realm.executeTransaction {
            template.title = titleInput.text.toString()
            if(template.id.isNullOrEmpty()) {
                template.id = UUID.randomUUID().toString()
                template.order = realm.where(Template::class.java).max("order")?.toInt()?.plus(1) ?: 0
            }
            realm.insertOrUpdate(template)
        }
        finish()
    }

    private fun delete() {
        realm.executeTransaction {
            realm.where(Template::class.java).equalTo("id", template.id)
                    .findFirst()?.deleteFromRealm()
        }
        finish()
    }

    override fun onBackPressed() {
        confirm()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}