package com.ayaan.twelvepages.manager

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.ayaan.twelvepages.App.Companion.resource
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.l
import com.pixplicity.easyprefs.library.Prefs
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object ColorManager {
    val colorPaletteSize = 10
    var primaryColorPack = ColorPack.values()[Prefs.getInt("primaryColorPack", 0)]
    var packs = ArrayList<ColorPack>()
    var recentPack = ArrayList<Int>()

    init {
        Prefs.getString("colorPacks",
                "${ColorPack.BASIC.name},${ColorPack.SPRING.name},${ColorPack.SUMMER.name}" +
                        ",${ColorPack.FALL.name},${ColorPack.WINTER.name}")
                .split(",")
                .mapTo(packs) { ColorPack.valueOf(it) }
        Prefs.getString("recentColorPack", null)?.let { recentColorPack ->
            recentColorPack.split(",")
                    .mapTo(recentPack) {
                        it.toInt()
                    }
        }
    }

    enum class ColorPack(val titleId: Int, val coverImgId: Int, val items: Array<Int>){
        BASIC(R.string.default_color_palette_name, R.drawable.color_palette_0,
                resource.getStringArray(R.array.colors).map { Color.parseColor(it) }.toTypedArray()),
        SPRING(R.string.apring, R.drawable.color_palette_1,
                resource.getStringArray(R.array.colors_spring).map { Color.parseColor(it) }.toTypedArray()),
        SUMMER(R.string.summer, R.drawable.color_palette_2,
                resource.getStringArray(R.array.colors_summer).map { Color.parseColor(it) }.toTypedArray()),
        FALL(R.string.fall, R.drawable.color_palette_3,
                resource.getStringArray(R.array.colors_fall).map { Color.parseColor(it) }.toTypedArray()),
        WINTER(R.string.winter, R.drawable.color_palette_4,
                resource.getStringArray(R.array.colors_winter).map { Color.parseColor(it) }.toTypedArray())
    }

    fun getColor(colorKey: Int) = ColorPack.values()[colorKey / colorPaletteSize].items[colorKey % colorPaletteSize]
    fun getFontColor(color: Int) = if(ColorUtils.calculateLuminance(color) < 0.8f) AppTheme.background else AppTheme.secondaryText
    fun getColorKey(color: Int): Int {
        val colors = primaryColorPack.items
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
                colorKey = primaryColorPack.ordinal * colorPaletteSize + i
                min_result = result
            }
        }
        return colorKey
    }

    fun updateRecentColor(colorKey: Int) {
        recentPack.remove(colorKey)
        recentPack.add(0, colorKey)
        if(recentPack.size > 20) {
            recentPack.removeAt(recentPack.lastIndex)
        }
        Prefs.putString("recentColorPack", recentPack.joinToString(","))
    }

    fun getPackIndex(colorKey: Int): Int {
        val pack = ColorPack.values()[colorKey / colorPaletteSize]
        val index = packs.indexOf(pack)
        if(index >= 0) {
            return index + 1
        }
        return 0
    }

    fun saveCurrentPack() {
        Prefs.putString("colorPacks", packs.joinToString(","){it.name})
    }

    fun deletePack(pack: ColorPack) {
        packs.remove(pack)
        saveCurrentPack()
    }

}