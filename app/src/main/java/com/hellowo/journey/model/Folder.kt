package com.hellowo.journey.model

import com.hellowo.journey.AppRes
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Folder(@PrimaryKey var id: String? = null,
                  var name: String? = null,
                  var color: Int = AppRes.primaryColor,
                  var type: Int = 0,
                  var coverImg: ByteArray? = null): RealmObject()