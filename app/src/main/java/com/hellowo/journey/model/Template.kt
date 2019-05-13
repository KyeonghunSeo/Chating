package com.hellowo.journey.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject

open class Template(@PrimaryKey var id: Int = -1,
                    var title: String? = null,
                    var type: Int = 0,
                    var colorKey: Int = -1,
                    var style: Int = 0,
                    var inCalendar: Boolean = true,
                    var folder: Folder? = null,
                    var tags: RealmList<Tag> = RealmList(),
                    var order: Int = 0): RealmObject() {

    override fun equals(other: Any?): Boolean {
        other as Template

        if (id != other.id) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (colorKey != other.colorKey) return false
        if (style != other.style) return false
        if (inCalendar != other.inCalendar) return false
        if (folder != other.folder) return false
        if (tags != other.tags) return false

        return true
    }

    override fun toString(): String {
        return "Template(id=$id, title=$title, type=$type, colorKey=$colorKey, style=$style, inCalendar=$inCalendar, folder=$folder, tags=$tags, order=$order)"
    }

}