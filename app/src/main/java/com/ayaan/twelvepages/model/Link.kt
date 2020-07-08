package com.ayaan.twelvepages.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Link(@PrimaryKey var id: String? = UUID.randomUUID().toString(),
                var type: Int = 0,
                var title: String? = null,
                var strParam0: String? = null,
                var strParam1: String? = null,
                var strParam2: String? = null,
                var intParam0: Int = 0,
                var intParam1: Int = 0,
                var intParam2: Int = 0,
                var properties: String? = null): RealmObject() {
    constructor(link: Link) : this(link.id, link.type, link.title, link.strParam0, link.strParam1,
            link.strParam2, link.intParam0, link.intParam1, link.intParam2, link.properties)

    enum class Type { WEB, IMAGE, VOICE, COUNTDOWN, CHECKLIST, PERCENTAGE, STICKER, BACKGROUND }

    override fun toString(): String {
        return "Link(id=$id, type=$type, title=$title, properties=$properties, intParam1=$intParam1)"
    }

}