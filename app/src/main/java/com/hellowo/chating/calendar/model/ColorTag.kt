package com.hellowo.chating.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class ColorTag(@PrimaryKey var id: Int = -1,
                    var title: String? = null,
                    var color: Int = Color.BLACK,
                    var order: Int = 0): RealmObject()