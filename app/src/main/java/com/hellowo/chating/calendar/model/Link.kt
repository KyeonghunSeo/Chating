package com.hellowo.chating.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Link(@PrimaryKey var id: String? = null,
                var type: Int = 0,
                var title: String? = null,
                var url: String? = null,
                var data: String? = null): RealmObject()