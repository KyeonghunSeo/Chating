package com.ayaan.twelvepages

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.ayaan.twelvepages.App.Companion.resource
import com.pixplicity.easyprefs.library.Prefs

object AppTheme {
    var thinCFont: Typeface = Typeface.DEFAULT
    var regularCFont: Typeface = Typeface.DEFAULT
    var boldCFont: Typeface = Typeface.DEFAULT
    var thinFont: Typeface = Typeface.DEFAULT
    var regularFont: Typeface = Typeface.DEFAULT
    var boldFont: Typeface = Typeface.DEFAULT

    var selectableItemBackground = 0
    var background = 0
    var backgroundDark = 0
    var backgroundAlpha = 0
    var primary = 0
    var primaryText = 0
    var secondaryText = 0
    var disableText = 0
    var line = 0
    var lightLine = 0
    var red = 0
    var blue = 0
    var icon = 0

    lateinit var hightlightCover: Drawable
    lateinit var blankDrawable: Drawable

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
        background = resource.getColor(R.color.background)
        backgroundDark = resource.getColor(R.color.backgroundDark)
        backgroundAlpha = resource.getColor(R.color.whiteAlpha)
        primary = resource.getColor(R.color.colorPrimary)
        primaryText = resource.getColor(R.color.primaryText)
        secondaryText = resource.getColor(R.color.secondaryText)
        disableText = resource.getColor(R.color.disableText)
        line = resource.getColor(R.color.line)
        lightLine = resource.getColor(R.color.light_line)
        red = resource.getColor(R.color.red)
        blue = resource.getColor(R.color.blue)
        icon = resource.getColor(R.color.iconTint)
        hightlightCover = resource.getDrawable(R.drawable.highlightcover)
        blankDrawable = resource.getDrawable(R.drawable.blank)
        thinCFont = ResourcesCompat.getFont(context, R.font.thin_c)!!
        regularCFont = ResourcesCompat.getFont(context, R.font.regular_c)!!
        boldCFont = ResourcesCompat.getFont(context, R.font.bold_c)!!
        thinFont = ResourcesCompat.getFont(context, R.font.thin)!!
        regularFont = ResourcesCompat.getFont(context, R.font.regular)!!
        boldFont = ResourcesCompat.getFont(context, R.font.bold)!!
    }
}