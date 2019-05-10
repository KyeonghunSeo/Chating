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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder

        if (id != other.id) return false
        if (name != other.name) return false
        if (color != other.color) return false
        if (type != other.type) return false
        if (properties != other.properties) return false
        if (order != other.order) return false

        return true
    }
}