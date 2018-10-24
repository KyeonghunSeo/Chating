package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Link(@PrimaryKey var id: String? = null,
                var type: Int = 0,
                var title: String? = null,
                var url: String? = null,
                var data: String? = null): RealmObject()