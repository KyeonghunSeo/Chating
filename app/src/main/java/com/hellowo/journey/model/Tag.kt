package com.hellowo.journey.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Tag(@PrimaryKey var id: String? = null,
               var order: Int = 0): RealmObject() {

    override fun toString(): String {
        return "Tag(id=$id, order=$order)"
    }
}