package com.hellowo.chating.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User(@PrimaryKey var id: String? = null): RealmObject()