package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.showDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_edit_folder.*
import java.util.*


class EditFolderDialog(private val activity: Activity, private val folder: Folder,
                       private val onResult: (Boolean) -> Unit) : Dialog(activity) {
    val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_edit_folder)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        titleText.text = if (folder.id.isNullOrEmpty()) context.getString(R.string.create_folder)
        else context.getString(R.string.edit_folder)

        if (folder.id.isNullOrEmpty()){
            titleText.text = context.getString(R.string.create_folder)

            cancelBtn.setOnClickListener {
                onResult.invoke(false)
                dismiss()
            }
        }else {
            titleText.text = context.getString(R.string.edit_folder)
            if(!folder.name.isNullOrEmpty()){
                input.setText(folder.name)
                input.setSelection(folder.name?.length ?: 0)
            }

            cancelBtn.setTextColor(AppTheme.redColor)
            cancelBtn.text = context.getString(R.string.delete)
            cancelBtn.setOnClickListener {
                showDialog(CustomDialog(activity, context.getString(R.string.delete_folder),
                        context.getString(R.string.delete_folder_sub), null) { result, _, _ ->
                    if(result) {
                        realm.executeTransaction {
                            realm.where(Record::class.java).equalTo("folder.id", folder.id)
                                    .findAll().deleteAllFromRealm()
                            realm.where(Folder::class.java).equalTo("id", folder.id)
                                    .findFirst()?.deleteFromRealm()
                        }
                        onResult.invoke(false)
                        dismiss()
                    }
                }, true, true, true, false)
            }
        }

        confirmBtn.setOnClickListener {
            realm.executeTransaction {
                if(folder.id.isNullOrEmpty()) {
                    realm.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                        name = input.text.toString()
                        order = realm.where(Folder::class.java).max("order")?.toInt()?.plus(1) ?: 0
                        type = 1
                    }
                }else {
                    realm.where(Folder::class.java).equalTo("id", folder.id).findFirst()?.let {
                        it.name = input.text.toString()
                    }
                }
            }
            onResult.invoke(true)
            dismiss()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        realm.close()
    }
}
