package com.hellowo.journey.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ColorTag(@PrimaryKey var id: Int = -1,
                    var title: String? = null,
                    var color: Int = Color.BLACK,
                    var fontColor: Int = Color.WHITE,
                    var order: Int = 0): RealmObject()