package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.R

object SymbolManager{

    val stamps = arrayOf(
            R.drawable.add
    )

    enum class Symbol(val resId: Int) {
        DOT(R.drawable.blank),
        IDEA(R.drawable.idea),
        NOTE(R.drawable.note),
        STAR(R.drawable.star),
        HEART(R.drawable.heart),
        RANGE(R.drawable.range)
    }

    fun getSymbolResId(symbol: String?): Int {
        return if (symbol == null) {
            R.drawable.blank
        } else {
            return try {
                Symbol.valueOf(symbol).resId
            } catch (ignored: Exception) {
                R.drawable.blank
            }
        }
    }
}