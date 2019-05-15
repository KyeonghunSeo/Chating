package com.hellowo.journey.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject

open class Template(@PrimaryKey var id: String? = null,
                    var title: String? = null,
                    var type: Int = 0,
                    var colorKey: Int = -1,
                    var style: Int = 0,
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
        if (folder != other.folder) return false
        if (tags != other.tags) return false

        return true
    }

    fun copy(data: Template) {
        id = data.id
        title = data.title
        type = data.type
        colorKey = data.colorKey
        style = data.style
        data.tags.forEach {
            tags.add(Tag(it.id))
        }
        data.folder?.let { folder = Folder(it) }
        order = data.order
    }

    override fun toString(): String {
        return "Template(id=$id, title=$title, type=$type, colorKey=$colorKey, style=$style, folder=$folder, tags=$tags, order=$order)"
    }

}