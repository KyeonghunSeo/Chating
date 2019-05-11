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
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + type
        result = 31 * result + colorKey
        result = 31 * result + style
        result = 31 * result + inCalendar.hashCode()
        result = 31 * result + (folder?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        result = 31 * result + order
        return result
    }

    override fun toString(): String {
        return "Template(id=$id, title=$title, type=$type, colorKey=$colorKey, style=$style, inCalendar=$inCalendar, folder=$folder, tags=$tags, order=$order)"
    }

}