package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AppUser(@PrimaryKey var id: String? = null,
                   var profileImg: ByteArray? = null,
                   var profileImgUrl: String? = null): RealmObject() {
}