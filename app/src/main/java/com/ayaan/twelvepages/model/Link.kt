package com.ayaan.twelvepages.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Link(@PrimaryKey var id: String? = UUID.randomUUID().toString(),
                var type: Int = 0,
                var title: String? = null,
                var properties: String? = null): RealmObject() {
    enum class Type { WEB, IMAGE, VOICE, DDAY, CHECKLIST, PERCENTAGE }

    override fun toString(): String {
        return "Link(id=$id, type=$type, title=$title, properties=$properties)"
    }

}