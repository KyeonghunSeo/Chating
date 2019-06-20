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

    enum class ColorPalette(val titleId: Int, val coverImgId: Int, val colors: Array<Int>){
        BASIC(R.string.default_color_palette_name, R.drawable.color_palette_0, resource.getStringArray(R.array.colors).map { Color.parseColor(it) }.toTypedArray()),
        SPRING(R.string.apring, R.drawable.color_palette_1, resource.getStringArray(R.array.colors_spring).map { Color.parseColor(it) }.toTypedArray()),
        SUMMER(R.string.summer, R.drawable.color_palette_2, resource.getStringArray(R.array.colors_summer).map { Color.parseColor(it) }.toTypedArray()),
        FALL(R.string.fall, R.drawable.color_palette_3,
                arrayOf(Color.parseColor("#CF9667"),
                        Color.parseColor("#5F261E"),
                        Color.parseColor("#DCD9CF"),
                        Color.parseColor("#67564C"),
                        Color.parseColor("#D9C990"),
                        Color.parseColor("#CF9667"),
                        Color.parseColor("#5F261E"),
                        Color.parseColor("#DCD9CF"),
                        Color.parseColor("#67564C"),
                        Color.parseColor("#D9C990"),
                        Color.parseColor("#000000"))),
        WINTER(R.string.winter, R.drawable.color_palette_4,
                arrayOf(Color.parseColor("#4C8C8C"),
                        Color.parseColor("#F1EFEA"),
                        Color.parseColor("#2F3D41"),
                        Color.parseColor("#CAC7BD"),
                        Color.parseColor("#626152"),
                        Color.parseColor("#4C8C8C"),
                        Color.parseColor("#F1EFEA"),
                        Color.parseColor("#2F3D41"),
                        Color.parseColor("#CAC7BD"),
                        Color.parseColor("#626152"),
                        Color.parseColor("#000000")))
    }

    val colorPaletteSize = 10
    var colorPalette = ColorPalette.values()[Prefs.getInt("colorPalette", 0)]

    fun getColor(colorKey: Int) = ColorPalette.values()[colorKey / colorPaletteSize].colors[colorKey % colorPaletteSize]
    fun getFontColor(color: Int) = if(ColorUtils.calculateLuminance(color) < 0.8f) background else secondaryText
    fun getColorKey(color: Int): Int {
        val colors = colorPalette.colors
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
                colorKey = colorPalette.ordinal * colorPaletteSize + i
                min_result = result
            }
        }
        return colorKey
    }
}