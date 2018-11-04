package com.hellowo.journey.model

import android.graphics.Color
import com.hellowo.journey.AppRes
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Template(@PrimaryKey var id: Int = -1,
                    var title: String? = null,
                    var type: Int = 0,
                    var color: Int = AppRes.primaryText,
                    var fontColor: Int = Color.WHITE,
                    var style: Int = 0,
                    var inCalendar: Boolean = true,
                    var tags: RealmList<Tag> = RealmList(),
                    var order: Int = 0): RealmObject()