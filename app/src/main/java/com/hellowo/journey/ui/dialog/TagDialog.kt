package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TagAdapter
import com.hellowo.journey.model.Tag
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.showDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_tag.*
import kotlin.collections.ArrayList


class TagDialog(val activity: Activity, val items: ArrayList<Tag>,
                private val onResult: (ArrayList<Tag>) -> Unit) : Dialog(activity) {
    val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_tag)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        tagText.text = items.joinToString("") { "#${it.id}" }

        addBtn.setOnClickListener { createAndAddTag() }
        tagInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) { createAndAddTag() }
            return@setOnEditorActionListener false
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TagAdapter(realm.where(Tag::class.java).findAllAsync(), items) { action, tag ->
            when(action) {
                0 -> {
                    if(items.any { it.id == tag.id }){
                        items.remove(items.first { it.id == tag.id })
                    }else {
                        items.add(realm.copyFromRealm(tag))
                    }
                }
                1 -> {
                    showDialog(CustomDialog(activity, context.getString(R.string.delete_tag),
                            context.getString(R.string.delete_tag_sub), null) { result, _, _ ->
                        if(result) {
                            deleteTag(tag)
                        }
                    }, true, true, true, false)
                }
            }
            tagText.text = items.joinToString("") { "#${it.id}" }
            recyclerView.adapter?.notifyDataSetChanged()
        }

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            createTag()
            onResult.invoke(items)
            dismiss()
        }
    }

    private fun createAndAddTag() {
        createTag()
        if(!tagInput.text.isNullOrBlank()){
            tagText.text = items.joinToString("") { "#${it.id}" }
            recyclerView.adapter?.notifyDataSetChanged()
            tagInput.setText("")
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
        }
        tagText.text = items.joinToString("") { "#${it.id}" }
    }

    private fun createTag() {
        if(!tagInput.text.isNullOrBlank()){
            val id = tagInput.text.toString()
            realm.executeTransaction {
                var tag = realm.where(Tag::class.java)
                        .equalTo("id", id)
                        .findFirst()
                if(tag == null) {
                    tag = realm.createObject(Tag::class.java, id)
                    val order = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                    tag.order = order + 1
                }
            }
            if(!items.any { it.id == id }){
                items.add(Tag(id))
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        realm.close()
    }

}
