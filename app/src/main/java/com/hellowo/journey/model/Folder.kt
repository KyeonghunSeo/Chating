package com.hellowo.journey.model

import com.hellowo.journey.AppTheme
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Folder(@PrimaryKey var id: String? = null,
                  var name: String? = null,
                  var color: Int = AppTheme.primaryColor,
                  var type: Int = 0,
                  var properties: String? = null,
                  var order: Int = 0): RealmObject() {

    constructor(folder: Folder) : this(folder.id, folder.name, folder.color, folder.type, folder.properties, folder.order)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder

        if (id != other.id) return false
        if (name != other.name) return false
        if (color != other.color) return false
        if (type != other.type) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + color
        result = 31 * result + type
        result = 31 * result + (properties?.hashCode() ?: 0)
        result = 31 * result + order
        return result
    }

    override fun toString(): String {
        return "Folder(id=$id, name=$name, color=$color, type=$type, properties=$properties, order=$order)"
    }

    fun isCalendar() = type == 0
    fun isNote() = type == 1

}