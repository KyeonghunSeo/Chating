package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.container_edit_folder.*
import java.util.*


class EditFolderDialog(private val activity: Activity, private val folder: Folder,
                       private val onResult: (Boolean) -> Unit) : BaseDialog(activity) {
    val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_edit_folder, dpToPx(300))
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        titleText.text = if (folder.id.isNullOrEmpty()) context.getString(R.string.create_folder)
        else context.getString(R.string.edit_folder)
        titleIcon.setImageResource(R.drawable.tab)

        if (folder.id.isNullOrEmpty()){
            typeLy.visibility = View.VISIBLE
            titleText.text = context.getString(R.string.create_folder)
            cancelBtn.setOnClickListener {
                onResult.invoke(false)
                dismiss()
            }
            calendarBtn.setOnClickListener {
                folder.type = 0
                checkView(calendarBtn)
                uncheckView(noteBtn)
            }
            noteBtn.setOnClickListener {
                folder.type = 1
                checkView(noteBtn)
                uncheckView(calendarBtn)
            }
        }else {
            typeLy.visibility = View.GONE
            titleText.text = context.getString(R.string.edit_folder)
            if(!folder.name.isNullOrEmpty()){
                input.setText(folder.name)
                input.setSelection(folder.name?.length ?: 0)
            }

            cancelBtn.setTextColor(AppTheme.red)
            cancelBtn.text = context.getString(R.string.delete)
            cancelBtn.setOnClickListener {
                val count = realm.where(Record::class.java).equalTo("folder.id", folder.id).count()
                showDialog(CustomDialog(activity, context.getString(R.string.delete_folder),
                        String.format(context.getString(R.string.delete_folder_sub), count), null) { result, _, _ ->
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
                    val newfolder = realm.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                        name = input.text.toString()
                        order = realm.where(Folder::class.java).max("order")?.toInt()?.plus(1) ?: 0
                        type = folder.type
                    }
                    rootLy.postDelayed({ MainActivity.getViewModel()?.setTargetFolder(newfolder) }, 300)
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
