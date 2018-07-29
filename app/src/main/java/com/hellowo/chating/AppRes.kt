package com.hellowo.chating

import android.content.Context
import android.util.TypedValue

object AppRes {
    var selectableItemBackground = 0
    val statusBarHeight = dpToPx(25)
    var topBarHeight = dpToPx(70)
    var bottomBarHeight = dpToPx(50)

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
    }
}