package com.hellowo.journey.manager

import android.graphics.Color
import com.hellowo.journey.App
import com.hellowo.journey.R
import com.hellowo.journey.model.Folder
import io.realm.Realm

object FolderManager {

    fun getPrimaryFolder() : Folder? {
        val realm = Realm.getDefaultInstance()
        var folder = realm.where(Folder::class.java).equalTo("id", "primary").findFirst()
        if(folder == null) {
            realm.executeTransaction {
                folder = realm.createObject(Folder::class.java, "primary").apply {
                    name = App.context.getString(R.string.keep)
                }
            }
        }
        realm.close()
        return folder
    }

}