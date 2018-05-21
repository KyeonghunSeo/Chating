package com.hellowo.chating.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class ChatRoom(@PrimaryKey var id: String? = null,
                    @Required var name: String? = null,
                    @Required var timestamp: Date? = null,
                    var messages: RealmList<Message>? = null): RealmObject()