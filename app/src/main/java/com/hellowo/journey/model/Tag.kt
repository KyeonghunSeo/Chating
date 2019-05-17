package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Tag(@PrimaryKey var id: String? = null,
               var title: String? = null,
               var order: Int = 0): RealmObject() {
    constructor(tag: Tag) : this(tag.id, tag.title, tag.order)

    override fun toString(): String {
        return "Tag(id=$id, title=$title, order=$order)"
    }
}