package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.ui.view.TagView.Companion.MODE_CHECK
import com.ayaan.twelvepages.ui.view.TagView.Companion.MODE_EDIT
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_tag.*
import java.util.*


class TagDialog(val activity: Activity, val items: ArrayList<Tag>,
                private val onResult: (ArrayList<Tag>) -> Unit) : Dialog(activity) {
    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
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
                editBtn.setTextColor(AppTheme.blue)
                editBtn.text = context.getString(R.string.edit_done)
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
                toast(R.string.already_exist_tag, R.drawable.info)
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
                toast(R.string.already_exist_tag, R.drawable.info)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        realm.close()
    }

}
