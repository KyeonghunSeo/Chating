package com.hellowo.journey.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Tag(@PrimaryKey var id: String? = null,
               var type: Int = 0,
               var color: Int = Color.TRANSPARENT): RealmObject()