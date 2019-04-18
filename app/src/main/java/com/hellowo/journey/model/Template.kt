package com.hellowo.journey.model

import android.graphics.Color
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.AppTheme
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject
import java.lang.Exception

open class Template(@PrimaryKey var id: Int = -1,
                    var title: String? = null,
                    var type: Int = 0,
                    var colorKey: Int = -1,
                    var style: Int = 0,
                    var inCalendar: Boolean = true,
                    var tags: RealmList<Tag> = RealmList(),
                    var order: Int = 0,
                    var options: String = JSONObject().toString()): RealmObject() {

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
        if (tags != other.tags) return false

        return true
    }
}