package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TagAdapter
import com.hellowo.journey.l
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.startDialogShowAnimation
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_tag.*
import java.util.*
import kotlin.collections.ArrayList


class TagDialog(activity: Activity, timeObject: TimeObject,
                private val onResult: (ArrayList<Tag>) -> Unit) : Dialog(activity) {
    val items = ArrayList<Tag>()
    val realm = Realm.getDefaultInstance()

    init {
        items.addAll(timeObject.tags)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tag)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        tagText.text = items.joinToString("") { "#${it.id}" }

        tagInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
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

                    items.add(Tag(id))
                    tagText.text = items.joinToString("") { "#${it.id}" }
                    recyclerView.adapter?.notifyDataSetChanged()
                    tagInput.setText("")
                }
            }
            return@setOnEditorActionListener false
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TagAdapter(realm.where(Tag::class.java).findAllAsync(), items) { tag ->
            if(items.any { it.id == tag.id }){
                items.remove(items.first { it.id == tag.id })
            }else {
                items.add(realm.copyFromRealm(tag))
            }
            tagText.text = items.joinToString("") { "#${it.id}" }
            recyclerView.adapter?.notifyDataSetChanged()
        }

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            onResult.invoke(items)
            dismiss()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        realm.close()
    }

}
