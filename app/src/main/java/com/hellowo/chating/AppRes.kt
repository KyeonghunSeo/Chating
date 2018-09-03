package com.hellowo.chating

import android.content.Context
import android.util.TypedValue

object AppRes {
    var selectableItemBackground = 0
    var unselectedColor = 0

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
        unselectedColor = context.resources.getColor(R.color.grey)
    }
}