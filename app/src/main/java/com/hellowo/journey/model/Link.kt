package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Link(@PrimaryKey var id: String? = null,
                var type: Int = 0,
                var title: String? = null,
                var properties: String? = null,
                var data: ByteArray? = null): RealmObject() {
    enum class Type { WEB, IMAGE, VOICE }

    override fun toString(): String {
        return "Link(id=$id, type=$type, title=$title, properties=$properties)"
    }

}