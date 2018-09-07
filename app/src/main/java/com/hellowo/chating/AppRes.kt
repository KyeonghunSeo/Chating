package com.hellowo.chating

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.graphics.drawable.Drawable
import java.text.SimpleDateFormat


object AppRes {
    var selectableItemBackground = 0
    var unselectedColor = 0
    var starDrawable: Drawable? = null

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
        unselectedColor = context.resources.getColor(R.color.grey)
        starDrawable = context.resources.getDrawable(R.drawable.ic_outline_star_border)
    }
}