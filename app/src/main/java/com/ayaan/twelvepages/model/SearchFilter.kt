package com.ayaan.twelvepages.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SearchFilter(@PrimaryKey var id: String? = null,
                        var filter: String? = null,
                        var order: Int = 0): RealmObject()