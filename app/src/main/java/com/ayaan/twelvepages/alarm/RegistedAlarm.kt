package com.ayaan.twelvepages.alarm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RegistedAlarm(@PrimaryKey var recordId: String? = null,
                         var requestCode: Int = -1): RealmObject()