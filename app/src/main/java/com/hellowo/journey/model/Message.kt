package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Message(@PrimaryKey var id: String? = null,
                   @Required var msg: String? = null,
                   @Required var timestamp: Date? = null): RealmObject()