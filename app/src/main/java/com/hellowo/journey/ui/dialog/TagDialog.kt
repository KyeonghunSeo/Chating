package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.model.Record
import com.hellowo.journey.model.Tag
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.view.TagView
import com.hellowo.journey.ui.view.TagView.Companion.MODE_CHECK
import com.hellowo.journey.ui.view.TagView.Companion.MODE_EDIT
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_tag.*
import java.util.*


class TagDialog(val activity: Activity, val items: ArrayList<Tag>,
                private val onResult: (ArrayList<Tag>) -> Unit) : BaseDialog(activity) {
    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tag)
        setLayout()
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.setOnClickListener { dismiss() }
        contentLy.setOnClickListener {}
        tagView.mode = MODE_CHECK
        tagView.onSelected = { tag, action ->
            if(tagView.mode == MODE_EDIT) {
                when(action) {
                    0 -> {
                        if(tag != null) {
                            showDialog(InputDialog(activity,
                                    context.getString(R.string.edit_tag), null,
                                    context.getString(R.string.enter_tag_name),
                                    tag.title?:"", true) { result, text ->
                                if(result) { editTag(tag, text) }
                            }, true, true, true, false)
                        }else {
                            showDialog(InputDialog(activity,
                                    context.getString(R.string.new_tag),
                                    context.getString(R.string.new_tag_sub),
                                    context.getString(R.string.enter_tag_name),
                                    "", true) { result, text ->
                                if(result) { createTag(text) }
                            }, true, true, true, false)
                        }
                    }
                    -1 -> {
                        if(tag != null) {
                            val count = realm.where(Record::class.java).equalTo("tags.id", tag.id).count()
                            showDialog(CustomDialog(activity,
                                    String.format(context.getString(R.string.delete_something), "#${tag.title}"),
                                    String.format(context.getString(R.string.delete_tag_sub), count),
                                    null) { result, _, _ ->
                                if(result) { deleteTag(tag) }
                            }, true, true, true, false)
                        }
                    }
                }
            }
        }
        realm.where(Tag::class.java).findAll()?.let { tagView.setItems(it, items) }

        editBtn.setOnClickListener {
            if(tagView.mode == MODE_CHECK) {
                editBtn.setTextColor(AppTheme.blueColor)
                editBtn.text = context.getString(R.string.done)
                tagView.startEditMode()
            }else {
                editBtn.setTextColor(AppTheme.secondaryText)
                editBtn.text = context.getString(R.string.edit)
                tagView.endEditMode()
            }
        }
        confirmBtn.setOnClickListener {
            onResult.invoke(tagView.checkedItems)
            dismiss()
        }
        setOnCancelListener {
            onResult.invoke(tagView.checkedItems)
        }
    }

    private fun deleteTag(tag: Tag) {
        if(items.any { it.id == tag.id }){
            items.remove(items.first { it.id == tag.id })
        }
        realm.executeTransaction {
            realm.where(Tag::class.java)
                    .equalTo("id", tag.id)
                    .findFirst()?.deleteFromRealm()
            tagView.deleteTag(tag)
        }
    }

    private fun createTag(title: String) {
        realm.executeTransaction {
            var tag = realm.where(Tag::class.java).equalTo("title", title).findFirst()
            if(tag == null) {
                tag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                val order = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                tag.title = title
                tag.order = order + 1
                items.add(realm.copyFromRealm(tag))
                tagView.addNewTag(tag)
            }else {
                Toast.makeText(context, context.getString(R.string.already_exist_tag), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editTag(oldTag: Tag, title: String) {
        realm.executeTransaction {
            var tag = realm.where(Tag::class.java).equalTo("title", title).findFirst()
            if(tag == null) {
                tag = realm.where(Tag::class.java).equalTo("id", oldTag.id).findFirst()
                if(tag != null) {
                    tag.title = title
                    oldTag.title = title
                    tagView.changeTag(oldTag)
                }
            }else {
                Toast.makeText(context, context.getString(R.string.already_exist_tag), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        realm.close()
    }

}
