package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.AppTheme
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Folder(@PrimaryKey var id: String? = null,
                  var name: String? = null,
                  var color: Int = AppTheme.primary,
                  var type: Int = 0,
                  var properties: String? = null,
                  var order: Int = 0): RealmObject() {

    constructor(folder: Folder) : this(folder.id, folder.name, folder.color, folder.type, folder.properties, folder.order)

    override fun equals(other: Any?): Boolean {
        other as Folder

        if (id != other.id) return false
        if (name != other.name) return false
        if (color != other.color) return false
        if (type != other.type) return false
        if (properties != other.properties) return false

        return true
    }

    override fun toString(): String {
        return "Folder(id=$id, name=$name, color=$color, type=$type, properties=$properties, order=$order)"
    }

    fun isCalendar() = type == 0
    fun isNote() = type == 1

}