package com.hellowo.journey

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.hellowo.journey.App.Companion.resource

object AppTheme {
    var thinFont: Typeface = Typeface.DEFAULT
    var regularFont: Typeface = Typeface.DEFAULT
    var boldFont: Typeface = Typeface.DEFAULT
    var digitFont: Typeface = Typeface.DEFAULT
    var digitBoldFont: Typeface = Typeface.DEFAULT
    var textFont: Typeface = Typeface.DEFAULT

    var selectableItemBackground = 0
    var almostWhite = 0
    var primaryColor = 0
    var primaryText = 0
    var secondaryText = 0
    var disableText = 0

    lateinit var starDrawable: Drawable
    lateinit var ideaDrawable: Drawable
    lateinit var hightlightCover: Drawable
    lateinit var blankDrawable: Drawable

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
        almostWhite = resource.getColor(R.color.almostWhite)
        primaryColor = resource.getColor(R.color.colorPrimary)
        primaryText = resource.getColor(R.color.primaryText)
        secondaryText = resource.getColor(R.color.secondaryText)
        disableText = resource.getColor(R.color.disableText)

        starDrawable = resource.getDrawable(R.drawable.sharp_star_rate_black_48dp)
        ideaDrawable = resource.getDrawable(R.drawable.sharp_perm_identity_black_48dp)
        hightlightCover = resource.getDrawable(R.drawable.highlightcover)
        blankDrawable = resource.getDrawable(R.drawable.blank)

        thinFont = ResourcesCompat.getFont(context, R.font.thin)!!
        regularFont = ResourcesCompat.getFont(context, R.font.regular)!!
        boldFont = ResourcesCompat.getFont(context, R.font.bold)!!
        digitFont = ResourcesCompat.getFont(context, R.font.digit)!!
        digitBoldFont = ResourcesCompat.getFont(context, R.font.digit_bold)!!
        textFont = ResourcesCompat.getFont(context, R.font.text)!!
    }
}