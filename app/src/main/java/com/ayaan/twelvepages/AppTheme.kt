package com.ayaan.twelvepages

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.ayaan.twelvepages.App.Companion.context
import com.ayaan.twelvepages.App.Companion.resource
import com.pixplicity.easyprefs.library.Prefs
import java.lang.Exception

object AppTheme {
    var thinCFont: Typeface = Typeface.DEFAULT
    var regularCFont: Typeface = Typeface.DEFAULT
    var boldCFont: Typeface = Typeface.DEFAULT
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

    lateinit var hightlightCover: Drawable
    lateinit var blankDrawable: Drawable

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
        BASIC(R.string.default_color_palette_name, R.drawable.bg,
                arrayOf(Color.parseColor("#d50000"),
                        Color.parseColor("#f4511e"),
                        Color.parseColor("#e67c73"),
                        Color.parseColor("#f6bf26"),
                        Color.parseColor("#33b679"),
                        Color.parseColor("#0b8043"),
                        Color.parseColor("#039be5"),
                        Color.parseColor("#3f51b5"),
                        Color.parseColor("#7986cb"),
                        Color.parseColor("#8e24aa"),
                        Color.parseColor("#606066"))),
        SPRING(R.string.apring, R.drawable.color_palette_1,
                arrayOf(Color.parseColor("#EC748F"),
                        Color.parseColor("#E7D06C"),
                        Color.parseColor("#9B7C56"),
                        Color.parseColor("#EAE8F1"),
                        Color.parseColor("#E39EA5"),
                        Color.parseColor("#406918"),
                        Color.parseColor("#E3A5CA"),
                        Color.parseColor("#84B158"),
                        Color.parseColor("#FAFAF7"),
                        Color.parseColor("#A1B587"),
                        Color.parseColor("#000000"))),
        SUMMER(R.string.summer, R.drawable.color_palette_2,
                arrayOf(Color.parseColor("#0C74B4"),
                        Color.parseColor("#30C1E0"),
                        Color.parseColor("#CEC9A9"),
                        Color.parseColor("#2E2F28"),
                        Color.parseColor("#2A798E"),
                        Color.parseColor("#6cb1ea"),
                        Color.parseColor("#647b30"),
                        Color.parseColor("#14625e"),
                        Color.parseColor("#6a797c"),
                        Color.parseColor("#3988c7"),
                        Color.parseColor("#012f20"))),
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

    val colorPaletteSize = 11
    var colorPalette = ColorPalette.values()[Prefs.getInt("colorPalette", 0)]

    fun getColor(colorKey: Int) = ColorPalette.values()[colorKey / colorPaletteSize].colors[colorKey % colorPaletteSize]
    fun getFontColor(color: Int) = if(ColorUtils.calculateLuminance(color) < 0.8f) backgroundColor else secondaryText
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