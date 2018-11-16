package com.hellowo.journey.model

import com.hellowo.journey.AppTheme
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Folder(@PrimaryKey var id: String? = null,
                  var name: String? = null,
                  var color: Int = AppTheme.primaryColor,
                  var type: Int = 0,
                  var tags: RealmList<Tag> = RealmList(),
                  var order: Int = 0,
                  var coverImg: ByteArray? = null): RealmObject()