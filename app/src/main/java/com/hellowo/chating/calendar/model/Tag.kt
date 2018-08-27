package com.hellowo.chating.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Tag(@PrimaryKey var id: String? = null,
               var type: Int = 0,
               var color: Int = Color.TRANSPARENT): RealmObject()