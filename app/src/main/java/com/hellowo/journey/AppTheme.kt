package com.hellowo.journey

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.hellowo.journey.App.Companion.resource
import java.lang.Exception

object AppTheme {
    var rFont: Typeface = Typeface.DEFAULT
    var bFont: Typeface = Typeface.DEFAULT
    var tFont: Typeface = Typeface.DEFAULT
    var thinFont: Typeface = Typeface.DEFAULT
    var regularFont: Typeface = Typeface.DEFAULT
    var boldFont: Typeface = Typeface.DEFAULT

    var selectableItemBackground = 0
    var backgroundColor = 0
    var backgroundDarkColor = 0
    var almostWhite = 0
    var primaryColor = 0
    var primaryText = 0
    var secondaryText = 0
    var disableText = 0
    var lineColor = 0
    var redColor = 0
    var blueColor = 0
    var iconColor = 0

    lateinit var starDrawable: Drawable
    lateinit var ideaDrawable: Drawable
    lateinit var hightlightCover: Drawable
    lateinit var blankDrawable: Drawable

    val colors = Array(20){0}
    val fontColors = Array(20){0}

    fun init(context: Context) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        selectableItemBackground = typedValue.resourceId
        backgroundColor = resource.getColor(R.color.background)
        backgroundDarkColor = resource.getColor(R.color.backgroundDark)
        almostWhite = resource.getColor(R.color.almostWhite)
        primaryColor = resource.getColor(R.color.colorPrimary)
        primaryText = resource.getColor(R.color.primaryText)
        secondaryText = resource.getColor(R.color.secondaryText)
        disableText = resource.getColor(R.color.disableText)
        lineColor = resource.getColor(R.color.line)
        redColor = resource.getColor(R.color.red)
        blueColor = resource.getColor(R.color.blue)
        iconColor = resource.getColor(R.color.iconTint)

        starDrawable = resource.getDrawable(R.drawable.sharp_star_rate_black_48dp)
        ideaDrawable = resource.getDrawable(R.drawable.sharp_perm_identity_black_48dp)
        hightlightCover = resource.getDrawable(R.drawable.highlightcover)
        blankDrawable = resource.getDrawable(R.drawable.blank)

        tFont = ResourcesCompat.getFont(context, R.font.avenir_medium)!!
        rFont = ResourcesCompat.getFont(context, R.font.avenir_medium)!!
        bFont = ResourcesCompat.getFont(context, R.font.avenir_black)!!
        thinFont = ResourcesCompat.getFont(context, R.font.thin)!!
        regularFont = ResourcesCompat.getFont(context, R.font.regular)!!
        boldFont = ResourcesCompat.getFont(context, R.font.bold)!!

        resource.getStringArray(R.array.colors).forEachIndexed { index, s ->
            colors[index] = Color.parseColor(s)
        }
        resource.getStringArray(R.array.font_colors).forEachIndexed { index, s ->
            fontColors[index] = Color.parseColor(s)
        }
    }

    fun getColor(colorKey: Int) : Int {
        return try{
            AppTheme.colors[colorKey]
        }catch (e: Exception){
            AppTheme.primaryText
        }
    }

    fun getFontColor(colorKey: Int) : Int {
        return try{
            AppTheme.fontColors[colorKey]
        }catch (e: Exception){
            Color.WHITE
        }
    }

    fun getColorKey(color: Int): Int {
        var colorKey = 0
        val red = color shr 16 and 0xff
        val green = color shr 8 and 0xff
        val blue = color and 0xff
        var c_red: Int
        var c_green: Int
        var c_blue: Int
        var min_result = Integer.MAX_VALUE
        var result: Int
        for (i in colors.indices) {
            c_red = colors[i] shr 16 and 0xff
            c_green = colors[i] shr 8 and 0xff
            c_blue = colors[i] and 0xff
            result = Math.abs(c_red - red) + Math.abs(c_green - green) + Math.abs(c_blue - blue)
            if (result < min_result) {
                colorKey = i
                min_result = result
            }
        }
        return colorKey
    }
}