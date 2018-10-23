package com.hellowo.journey.alarm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RegistedAlarm(@PrimaryKey var timeObjectId: String? = null,
                         var requestCode: Int = -1): RealmObject()